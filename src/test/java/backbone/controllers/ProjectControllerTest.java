package backbone.controllers;

import backbone.configs.BackboneException;
import backbone.configs.GlobalExceptionHandler;
import backbone.dto.FetchProjectRequest;
import backbone.dto.ProjectDto;
import backbone.dto.Res;
import backbone.models.Project;
import backbone.services.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import(GlobalExceptionHandler.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void fetchShouldReturnProjectsAndUseDefaultPageArguments() throws Exception {
        Project project = new Project();
        project.setId("p-1");
        project.setTitle("Project One");

        Res<List<Project>> serviceResponse = Res.<List<Project>>builder()
                .message("projects fetched successfully")
                .data(List.of(project))
                .build();

        when(projectService.fetchProjects(any(FetchProjectRequest.class), anyString())).thenReturn(serviceResponse);

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("projects fetched successfully"))
                .andExpect(jsonPath("$.data[0].id").value("p-1"))
                .andExpect(jsonPath("$.data[0].title").value("Project One"))
                .andExpect(jsonPath("$.timestamp").exists());

        ArgumentCaptor<FetchProjectRequest> requestCaptor = ArgumentCaptor.forClass(FetchProjectRequest.class);
        verify(projectService).fetchProjects(requestCaptor.capture(), anyString());

        assertThat(requestCaptor.getValue().getPage()).isEqualTo(0);
        assertThat(requestCaptor.getValue().getSize()).isEqualTo(10);
        assertThat(requestCaptor.getValue().getSearch()).isNull();
    }

    @Test
    void fetchShouldForwardSearchAndPaginationArguments() throws Exception {
        Res<List<Project>> serviceResponse = Res.<List<Project>>builder()
                .message("projects fetched successfully")
                .data(List.of())
                .build();

        when(projectService.fetchProjects(any(FetchProjectRequest.class), anyString())).thenReturn(serviceResponse);

        mockMvc.perform(get("/api/projects")
                        .param("search", "ai")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("projects fetched successfully"))
                .andExpect(jsonPath("$.data").isArray());

        ArgumentCaptor<FetchProjectRequest> requestCaptor = ArgumentCaptor.forClass(FetchProjectRequest.class);
        verify(projectService).fetchProjects(requestCaptor.capture(), anyString());

        assertThat(requestCaptor.getValue().getSearch()).isEqualTo("ai");
        assertThat(requestCaptor.getValue().getPage()).isEqualTo(2);
        assertThat(requestCaptor.getValue().getSize()).isEqualTo(5);
    }

    @Test
    void fetchProjectShouldReturnSingleProjectResponse() throws Exception {
        Project project = new Project();
        project.setId("p-2");
        project.setTitle("Project Two");

        when(projectService.fetchProject(eq("p-2"), anyString())).thenReturn(project);

        mockMvc.perform(get("/api/projects/{id}", "p-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("single project fetched successfully"))
                .andExpect(jsonPath("$.data.id").value("p-2"))
                .andExpect(jsonPath("$.data.title").value("Project Two"))
                .andExpect(jsonPath("$.path").value("/api/projects/p-2"));
    }

    @Test
    void fetchProjectShouldReturnProblemDetailWhenServiceThrowsBackboneException() throws Exception {
        when(projectService.fetchProject(eq("missing-id"), anyString()))
                .thenThrow(new BackboneException("PROJECT_NOT_FOUND", HttpStatus.NOT_FOUND, "not found"));

        mockMvc.perform(get("/api/projects/{id}", "missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("BackboneException"))
                .andExpect(jsonPath("$.detail").value("not found"))
                .andExpect(jsonPath("$.code").value("PROJECT_NOT_FOUND"))
                .andExpect(jsonPath("$.instance").value("/api/projects/missing-id"));
    }

    @Test
    void createProjectShouldReturnCreatedDraftResponse() throws Exception {
        ProjectDto dto = ProjectDto.builder().title("Fresh Title").description("Fresh Description").build();
        Project saved = new Project();
        saved.setId("p-3");
        saved.setTitle("Fresh Title");

        when(projectService.addProject(any(ProjectDto.class), anyString())).thenReturn(saved);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("project draft created successfully"))
                .andExpect(jsonPath("$.data.id").value("p-3"))
                .andExpect(jsonPath("$.path").value("/api/projects"));
    }

    @Test
    void updateProjectShouldReturnUpdatedProjectResponse() throws Exception {
        Project updatePayload = new Project();
        updatePayload.setTitle("Updated Title");
        updatePayload.setDescription("Updated Description");

        Project updated = new Project();
        updated.setId("p-4");
        updated.setTitle("Updated Title");

        when(projectService.updateProject(any(Project.class), eq("p-4"), anyString())).thenReturn(updated);

        mockMvc.perform(put("/api/projects/{id}", "p-4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("project updated successfully"))
                .andExpect(jsonPath("$.data.id").value("p-4"))
                .andExpect(jsonPath("$.path").value("/api/projects/p-4"));
    }

    @Test
    void deleteProjectShouldReturnSuccessResponse() throws Exception {
        Res<Void> serviceResponse = Res.<Void>builder().message("project details deleted successfyully").build();
        when(projectService.deleteProject(eq("p-5"), anyString())).thenReturn(serviceResponse);

        mockMvc.perform(delete("/api/projects/{id}", "p-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("project details deleted successfyully"));
    }

    @Test
    void deleteProjectShouldReturnInternalServerErrorForUnhandledExceptions() throws Exception {
        when(projectService.deleteProject(eq("p-6"), anyString()))
                .thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(delete("/api/projects/{id}", "p-6"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("internal server error"))
                .andExpect(jsonPath("$.detail").value("boom"))
                .andExpect(jsonPath("$.code").value("internal_server_error"));
    }

    @Test
    void fetchShouldReturnBadRequestWhenPageParameterIsInvalid() throws Exception {
        mockMvc.perform(get("/api/projects").param("page", "not-a-number"))
                .andExpect(status().isBadRequest());
    }
}
