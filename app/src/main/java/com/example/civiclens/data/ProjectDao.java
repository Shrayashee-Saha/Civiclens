package com.example.civiclens.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.civiclens.model.ProjectEvent;
import com.example.civiclens.model.ProjectMilestone;
import com.example.civiclens.model.ProjectObservation;
import com.example.civiclens.model.PublicProject;

import java.util.List;

@Dao
public interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY district, name")
    List<PublicProject> getAll();

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    PublicProject getById(String id);

    @Query("SELECT COUNT(*) FROM projects")
    int count();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProjects(List<PublicProject> projects);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMilestones(List<ProjectMilestone> milestones);

    @Query("SELECT * FROM project_milestones WHERE projectId = :projectId ORDER BY sortOrder")
    List<ProjectMilestone> getMilestones(String projectId);

    @Query("SELECT * FROM project_observations WHERE projectId = :projectId ORDER BY createdAt DESC")
    List<ProjectObservation> getObservations(String projectId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addObservation(ProjectObservation observation);

    @Query("SELECT * FROM project_events WHERE projectId = :projectId ORDER BY createdAt DESC")
    List<ProjectEvent> getEvents(String projectId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addEvent(ProjectEvent event);

    @Query("SELECT * FROM accountability_cases ORDER BY publicationDate DESC")
    List<com.example.civiclens.model.AccountabilityCase> getAccountabilityCases();

    @Query("DELETE FROM accountability_cases WHERE title LIKE '%cricket%' OR title LIKE '%football%' OR title LIKE '%match%' OR title LIKE '%bpl%' OR title LIKE '%ipl%' OR title LIKE '%sports%' OR title LIKE '%movie%' OR title LIKE '%film%' OR title LIKE '%gaza%' OR title LIKE '%israel%' OR title LIKE '%ukraine%' OR title LIKE '%biden%' OR title LIKE '%trump%' OR reportedConcern LIKE '%cricket%' OR reportedConcern LIKE '%football%' OR reportedConcern LIKE '%sports%' OR reportedConcern LIKE '%movie%' OR reportedConcern LIKE '%film%'")
    void purgeExcludedAccountabilityCases();

    @Query("SELECT * FROM accountability_cases WHERE projectId = :projectId ORDER BY publicationDate DESC")
    List<com.example.civiclens.model.AccountabilityCase> getAccountabilityCasesForProject(String projectId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAccountabilityCases(List<com.example.civiclens.model.AccountabilityCase> cases);

    @Query("SELECT * FROM project_contributions ORDER BY createdAt DESC")
    List<com.example.civiclens.model.ProjectContribution> getContributions();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addContribution(com.example.civiclens.model.ProjectContribution contribution);
}
