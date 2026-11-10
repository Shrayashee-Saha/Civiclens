package com.example.civiclens.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.civiclens.model.CivicCommunity;
import com.example.civiclens.model.CivicPost;
import com.example.civiclens.model.GroupMessage;

import java.util.List;

@Dao
public interface CommunityDao {

    @Query("SELECT * FROM civic_posts ORDER BY createdAt DESC")
    List<CivicPost> getAllPosts();

    @Query("SELECT * FROM civic_posts WHERE category = :cat ORDER BY createdAt DESC")
    List<CivicPost> getPostsByCategory(String cat);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPost(CivicPost post);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPosts(List<CivicPost> posts);

    @Query("SELECT * FROM civic_communities ORDER BY memberCount DESC")
    List<CivicCommunity> getAllCommunities();

    @Query("SELECT * FROM civic_communities WHERE id = :id LIMIT 1")
    CivicCommunity getCommunityById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCommunity(CivicCommunity community);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCommunities(List<CivicCommunity> communities);

    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY createdAt ASC")
    List<GroupMessage> getGroupMessages(String groupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(GroupMessage message);
}
