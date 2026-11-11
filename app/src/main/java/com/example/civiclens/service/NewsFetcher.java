package com.example.civiclens.service;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Xml;

import com.example.civiclens.BuildConfig;
import com.example.civiclens.model.AccountabilityCase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Multi-source Infrastructure News, GNews API & Geotagged Media Aggregator for CivicLens BD.
 */
public final class NewsFetcher {

    public static final String[] RSS_SOURCES = {
            "https://www.tbsnews.net/rss.xml",
            "https://www.thedailystar.net/frontpage/rss.xml",
            "https://en.prothomalo.com/feed/",
            "https://www.dhakatribune.com/rss.xml",
            "https://bdnews24.com/?widgetName=rssfeed",
            "https://unb.com.bd/rss/feed",
            "https://www.daily-bangladesh.com/rss/xml",
            "https://www.daily-sun.com/rss/feed",
            "https://www.newagebd.net/rss.xml",
            "https://www.bangladeshpost.net/rss.xml",
            "https://thefinancialexpress.com.bd/rss.xml",
            "https://www.ittefaq.com.bd/rss.xml"
    };

    private static final String[] EXCLUDED_KEYWORDS = {
          "sports"  ,"cricket", "soccer","hockey","football", "ipl", "bpl", "match", "stadium", "wicket", "runs", "trophy",
            "tournament", "championship", "messi", "ronaldo", "shakib", "tamim", "sports", "fifa",
            "score", "league", "movie", "film", "actor", "actress", "bollywood", "dhallywood",
            "hollywood", "cinema", "song", "album", "celebrity", "showbiz", "entertainment","france","europe","africa",
            "ukraine", "gaza", "israel", "putin", "biden", "trump", "middle east", "palestine"
    };

    public interface Callback {
        void onResult(List<AccountabilityCase> newsItems);
    }

    private NewsFetcher() { }

    public static String cleanPlainText(String rawHtml) {
        if (rawHtml == null || rawHtml.isEmpty()) {
            return "";
        }
        String text = rawHtml.replaceAll("(?i)<[^>]*>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();
        if (text.length() > 220) {
            text = text.substring(0, 220) + "...";
        }
        return text;
    }

    public static boolean isExcludedTopic(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase(Locale.US);
        for (String kw : EXCLUDED_KEYWORDS) {
            if (lower.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    public static void fetchAll(Callback callback) {
        new Thread(() -> {
            List<AccountabilityCase> results = new ArrayList<>();

            if (!TextUtils.isEmpty(BuildConfig.NEWS_API_KEY)) {
                List<AccountabilityCase> gnews = fetchGNewsApi(BuildConfig.NEWS_API_KEY);
                results.addAll(gnews);
            }

            for (String feedUrl : RSS_SOURCES) {
                try {
                    List<AccountabilityCase> rssItems = fetchSingleRss(feedUrl);
                    results.addAll(rssItems);
                } catch (Exception ignored) { }
            }

            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(results));
        }).start();
    }

    private static List<AccountabilityCase> fetchGNewsApi(String apiKey) {
        List<AccountabilityCase> list = new ArrayList<>();
        try {
            String queryUrl = "https://gnews.io/api/v4/search?q=Bangladesh+(infrastructure+OR+road+OR+bridge+OR+expressway+OR+metro+OR+drainage+OR+wetland+OR+audit+OR+cag+OR+wasa+OR+desco+OR+environment)&lang=en&max=20&apikey=" + apiKey;
            HttpURLConnection conn = (HttpURLConnection) new URL(queryUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject root = new JSONObject(sb.toString());
                JSONArray articles = root.optJSONArray("articles");
                if (articles != null) {
                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject a = articles.getJSONObject(i);
                        String rawTitle = a.optString("title", "Infrastructure News");
                        String rawDesc = a.optString("description", "");
                        if (isExcludedTopic(rawTitle + " " + rawDesc)) {
                            continue;
                        }

                        AccountabilityCase item = new AccountabilityCase();
                        item.id = "gnews_" + i;
                        item.title = cleanPlainText(rawTitle);
                        item.newsUrl = a.optString("url", "").trim();
                        item.imageUrl = a.optString("image", "");
                        item.publicationDate = a.optString("publishedAt", "").split("T")[0];
                        item.publisher = a.optJSONObject("source") != null ? a.optJSONObject("source").optString("name", "GNews") : "GNews";
                        item.reportedConcern = cleanPlainText(rawDesc);
                        item.investigationStatus = "Reported";
                        item.sourceStatus = "Geotagged Live Media Article";

                        applyGeotag(item, item.title + " " + item.reportedConcern);
                        list.add(item);
                    }
                }
            }
            conn.disconnect();
        } catch (Exception ignored) { }
        return list;
    }

    private static List<AccountabilityCase> fetchSingleRss(String feedUrl) {
        List<AccountabilityCase> list = new ArrayList<>();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(feedUrl).openConnection();
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                InputStream stream = conn.getInputStream();
                list = parseRss(stream, getPublisherName(feedUrl));
                stream.close();
            }
            conn.disconnect();
        } catch (Exception ignored) { }
        return list;
    }

    private static String getPublisherName(String url) {
        if (url.contains("tbsnews")) return "The Business Standard";
        if (url.contains("thedailystar")) return "The Daily Star";
        if (url.contains("prothomalo")) return "Prothom Alo";
        if (url.contains("dhakatribune")) return "Dhaka Tribune";
        if (url.contains("bdnews24")) return "bdnews24";
        if (url.contains("unb")) return "UNB";
        return "Bangladesh News";
    }

    private static List<AccountabilityCase> parseRss(InputStream stream, String publisher) throws Exception {
        List<AccountabilityCase> list = new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(stream, null);

        int eventType = parser.getEventType();
        String title = "";
        String link = "";
        String pubDate = "";
        String description = "";
        String imageUrl = "";
        boolean insideItem = false;
        int count = 0;

        while (eventType != XmlPullParser.END_DOCUMENT && count < 5) {
            String tagName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("item".equalsIgnoreCase(tagName)) {
                        insideItem = true;
                        title = "";
                        link = "";
                        pubDate = "";
                        description = "";
                        imageUrl = "";
                    } else if (insideItem) {
                        if ("title".equalsIgnoreCase(tagName)) {
                            title = parser.nextText();
                        } else if ("link".equalsIgnoreCase(tagName)) {
                            link = parser.nextText();
                        } else if ("pubDate".equalsIgnoreCase(tagName)) {
                            pubDate = parser.nextText();
                        } else if ("description".equalsIgnoreCase(tagName)) {
                            description = parser.nextText();
                        } else if ("enclosure".equalsIgnoreCase(tagName) || "media:content".equalsIgnoreCase(tagName)) {
                            imageUrl = parser.getAttributeValue(null, "url");
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if ("item".equalsIgnoreCase(tagName) && insideItem) {
                        insideItem = false;
                        String cleanT = cleanPlainText(title);
                        String cleanD = cleanPlainText(description);
                        if (isExcludedTopic(cleanT + " " + cleanD)) {
                            break;
                        }

                        AccountabilityCase item = new AccountabilityCase();
                        item.id = "rss_" + publisher.hashCode() + "_" + count;
                        item.title = cleanT;
                        item.newsUrl = link.trim();
                        item.imageUrl = imageUrl;
                        item.publicationDate = pubDate;
                        item.publisher = publisher;
                        item.reportedConcern = cleanD;
                        item.investigationStatus = "Reported";
                        item.sourceStatus = "Live Geotagged Media (" + publisher + ")";

                        applyGeotag(item, cleanT + " " + cleanD);
                        list.add(item);
                        count++;
                    }
                    break;
            }
            eventType = parser.next();
        }
        return list;
    }

    private static void applyGeotag(AccountabilityCase item, String text) {
        String lower = text.toLowerCase(Locale.US);
        if (lower.contains("khulna") || lower.contains("dumuria")) {
            item.locationName = "Dumuria, Khulna";
            item.latitude = 22.8088;
            item.longitude = 89.4300;
        } else if (lower.contains("gazipur") || lower.contains("tongi")) {
            item.locationName = "Tongi, Gazipur";
            item.latitude = 23.9888;
            item.longitude = 90.3800;
        } else if (lower.contains("sylhet")) {
            item.locationName = "Sylhet Sadar";
            item.latitude = 24.8949;
            item.longitude = 91.8687;
        } else if (lower.contains("chattogram") || lower.contains("chittagong") || lower.contains("patenga")) {
            item.locationName = "Patenga, Chattogram";
            item.latitude = 22.2333;
            item.longitude = 91.7833;
        } else if (lower.contains("barisal")) {
            item.locationName = "Barisal Sadar";
            item.latitude = 22.7010;
            item.longitude = 90.3535;
        } else if (lower.contains("rajshahi")) {
            item.locationName = "Rajshahi Sadar";
            item.latitude = 24.3745;
            item.longitude = 88.6042;
        } else {
            item.locationName = "Dhaka, Bangladesh";
            item.latitude = 23.8103;
            item.longitude = 90.4125;
        }
    }
}
