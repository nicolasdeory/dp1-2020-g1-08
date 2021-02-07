package org.springframework.samples.petclinic.web;

import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.samples.petclinic.PetclinicInitializer;
import org.springframework.samples.petclinic.config.TestUserWebConfig;
import org.springframework.samples.petclinic.config.TestWebConfig;
import org.springframework.samples.petclinic.configuration.GenericIdToEntityConverter;
import org.springframework.samples.petclinic.configuration.SecurityConfiguration;
import org.springframework.samples.petclinic.model.Belongs;
import org.springframework.samples.petclinic.model.Participation;
import org.springframework.samples.petclinic.model.Role;
import org.springframework.samples.petclinic.model.Team;
import org.springframework.samples.petclinic.model.UserTW;
import org.springframework.samples.petclinic.service.*;
import org.springframework.samples.petclinic.validation.ManyTeamOwnerException;
import org.springframework.samples.petclinic.validation.UserValidator;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

/**
 * Test class for {@link UserTWController}
 *
 */

@WebMvcTest(controllers = UserTWController.class,
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@WebAppConfiguration
@ContextConfiguration(classes = {TestWebConfig.class, SecurityConfiguration.class})
@Import(UserTWController.class)
public class UserTWControllerTests {
	
	
	private static final int TEST_USER_ID = 1;
	private static final int TEST_TEAM_ID = 3;

    @MockBean
    GenericIdToEntityConverter idToEntityConverter;

	@MockBean
	private UserTWService UserTWService;

    @MockBean
	private TeamService teamService;
    @MockBean
    private BelongsService belongsService;
    @MockBean
    private ParticipationService participationService;
    
    @MockBean
    private UserValidator userValidator;

//    @Autowired
//    private WebApplicationContext context;
//
//    @Autowired
//    private Filter springSecurityFilterChain;

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	private UserTW juan;

	private Team team;
	private List<UserTW> userList;
	private Collection<UserTW> userCol;
	private Collection<Participation> participationCol;
	private Collection<Belongs> belongsCol;
	@Autowired
	protected MockHttpSession mockSession;

//    @Before
//    public void setupTests() {
//        mockMvc = MockMvcBuilders
//            .webAppContextSetup(context)
//            .addFilters(springSecurityFilterChain)
//            .build();
//    }

	@BeforeEach
	void setup() {
		
		userCol=new ArrayList<>();
		belongsCol=new ArrayList<>();
		participationCol=new ArrayList<>();
		userList=new ArrayList<>();
		
		//Juan
		juan = new UserTW();
		juan.setId(TEST_USER_ID);
		juan.setName("Juan");
		juan.setLastname("Franklin");
		juan.setEmail("andrespuertas@cyber");
		juan.setPassword("123456789");
		juan.setRole(Role.team_owner);
		
		//Team
		team=new Team();
		team.setId(TEST_TEAM_ID);
		juan.setTeam(team);
		//Participation and belongs of George
		Participation participation=new Participation();
		participation.setUserTW(juan);
		participation.setIsProjectManager(true);
		Belongs belongs=new Belongs();
		belongs.setUserTW(juan);
		belongs.setIsDepartmentManager(true);
		belongsCol.add(belongs);
		participationCol.add(participation);
		given(belongsService.findUserBelongs(TEST_USER_ID)).willReturn(belongsCol);
		given(belongsService.findCurrentUserBelongs(TEST_USER_ID)).willReturn(belongsCol);
		given(participationService.findCurrentParticipationsUser(TEST_USER_ID)).willReturn(participationCol);
		given(participationService.findUserParticipations(TEST_USER_ID)).willReturn(participationCol);
		//Session
		mockSession.setAttribute("userId",TEST_USER_ID);
		mockSession.setAttribute("teamId",TEST_TEAM_ID);
		
		
		//given(this.mockSession.getAttribute("userID"),TEST_USER_ID)
		given(this.UserTWService.findUserById(TEST_USER_ID)).willReturn(juan);
		given(this.teamService.findTeamById(TEST_TEAM_ID)).willReturn(team);
		
		
		


	}

	@Test
	void testGetUsers() throws Exception {
		userCol.add(juan);
		userList=userCol.stream().collect(Collectors.toList());
		given(this.UserTWService.findUsersByTeam(TEST_TEAM_ID)).willReturn(userCol);
		String usersJson = objectMapper.writeValueAsString(userList);
		mockMvc.perform(get("/api/users").session(mockSession)).andExpect(status().is(200)).andExpect(content().json(usersJson));
	}
	@Test
	void testGetUser() throws Exception {
		
		Map<String,Object> m=new HashMap<>();
		m.put("user", juan);
		List<Belongs> lb = belongsCol.stream().collect(Collectors.toList());
		m.put("currentDepartments", lb);
		List<Participation> lp = participationCol.stream()
				.collect(Collectors.toList());
		m.put("currentProjects", lp);
		String userDeatailJson = objectMapper.writeValueAsString(m);
		Integer userId=TEST_USER_ID;
		String userIdString=userId.toString();
		mockMvc.perform(get("/api/user").session(mockSession).param("userId",userIdString )).andExpect(status().is(200)).andExpect(content().json(userDeatailJson));
	}
	@Test
	void testGetInvalidUser() throws Exception {
		mockMvc.perform(get("/api/user").session(mockSession).param("userId","2000" )).andExpect(status().is(400));
	}
	@Test
	void testGetUserDiferentTeam() throws Exception {
		UserTW george2=juan;
		george2.getTeam().setId(20);
		given(this.UserTWService.findUserById(TEST_USER_ID+1)).willReturn(juan);
		Integer userId=TEST_USER_ID+1;
		String userIdString=userId.toString();
		mockMvc.perform(get("/api/user").session(mockSession).param("userId",userIdString )).andExpect(status().is(400));
	}
	
	
    @Test
	void testPostUser() throws Exception {
		String georgejson = objectMapper.writeValueAsString(juan);
		mockMvc.perform(post("/api/user").session(mockSession).contentType(MediaType.APPLICATION_JSON).content(georgejson)).andExpect(status().is(200));
	}
    @Test
	void testPostMorethan1TeamOwner() throws Exception {
    	
    	UserTW george2=juan;
    	
    	doThrow(ManyTeamOwnerException.class).when(this.UserTWService).saveUser(george2);
		mockMvc.perform(post("/api/user").session(mockSession)).andExpect(status().is(400));
	}
    
    @Test
  	void testDeleteUser() throws Exception {
    	Integer userId=TEST_USER_ID;
		String userIdString=userId.toString();
  		mockMvc.perform(delete("/api/user").session(mockSession).param("userId",userIdString)).andExpect(status().is(200));
  	}
    
    @Test
    void testGetCredentials() throws Exception{
    	Map<String,Object> m=new HashMap<>();
    	m.put("isTeamManager", juan.getRole().equals(Role.team_owner));
		List<Belongs> lb = belongsCol.stream().collect(Collectors.toList());
		m.put("currentDepartments", lb);
		List<Participation> lp = participationCol.stream()
			.collect(Collectors.toList());
		m.put("currentProjects", lp);
    	Integer userId=TEST_USER_ID;
		String userIdString=userId.toString();
		String georgeCredentialsJson = objectMapper.writeValueAsString(m);
    	mockMvc.perform(get("/api/user/credentials").session(mockSession).param("userId",userIdString)).andExpect(status().is(200)).andExpect(content().json(georgeCredentialsJson));
    }
    @Test
	void testGetInvalidUserCredentials() throws Exception {
		mockMvc.perform(get("/api/user/credentials").session(mockSession).param("userId","2000" )).andExpect(status().is(400));
	}
	@Test
	void testGetUserCredentialsDiferentTeam() throws Exception {
		UserTW george2=juan;
		george2.getTeam().setId(20);
		given(this.UserTWService.findUserById(TEST_USER_ID+1)).willReturn(juan);
		Integer userId=TEST_USER_ID+1;
		String userIdString=userId.toString();
		mockMvc.perform(get("/api/user/credentials").session(mockSession).param("userId",userIdString )).andExpect(status().is(400));
	}
	
}