package com.decode.app.data.repository

import com.decode.app.data.db.ProjectDao
import com.decode.app.data.model.Project
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {

    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun getProject(id: Long): Project? = projectDao.getProjectById(id)

    suspend fun saveProject(project: Project): Long = projectDao.insertProject(project)

    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)

    suspend fun deleteProject(id: Long) = projectDao.deleteProjectById(id)

    suspend fun getCount(): Int = projectDao.getProjectCount()
}
