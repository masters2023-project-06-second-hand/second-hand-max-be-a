package com.codesquad.secondhand.domain.oauth.service;

import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.codesquad.secondhand.domain.member.entity.Member;
import com.codesquad.secondhand.domain.member.service.MemberQueryService;
import com.codesquad.secondhand.domain.token.entity.Token;
import com.codesquad.secondhand.domain.token.repository.TokenJpaRepository;

@SpringBootTest
class OAuth2SuccessHandlerTest {

	@Autowired
	OAuth2SuccessHandler oAuth2SuccessHandler;

	@Autowired
	HttpServletResponse AutoWiredResponse;

	@Autowired
	HttpServletRequest AutoWiredRequest;

	@Mock
	HttpServletRequest request;

	@Mock
	HttpServletResponse response;

	@Mock
	OAuth2User oAuth2User;

	@Mock
	OAuth2AuthenticationToken authentication;

	@MockBean
	MemberQueryService memberQueryService;

	@MockBean
	TokenJpaRepository tokenJpaRepository;

	@Test
	@DisplayName("naver로 oauth 로그인시 최초 로그인한 사람이라면, signuptoken 을 포함한 error 예외를 응답으로 보낸다.")
	void onAuthenticationSuccessWithProviderNaver() throws IOException {
		given(authentication.getAuthorizedClientRegistrationId()).willReturn("naver");
		given(authentication.getPrincipal()).willReturn(oAuth2User);
		given(oAuth2User.getAttributes()).willReturn(Map.of("response", Map.of("email", "admin@naver.com")));

		StringWriter stringWriter = getStringWriter();

		//when
		oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

		//then
		String responseBody = stringWriter.toString();
		Assertions.assertThat(responseBody).contains("signupToken");
		Assertions.assertThat(responseBody).contains("존재하지 않는 유저");
	}

	@Test
	@DisplayName("google로 oauth 로그인시 최초 로그인한 사람이라면, signuptoken 을 포함한 error 예외를 응답으로 보낸다.")
	void onAuthenticationSuccessWithProviderGoogle() throws IOException {
		given(authentication.getAuthorizedClientRegistrationId()).willReturn("google");
		given(authentication.getPrincipal()).willReturn(oAuth2User);
		given(oAuth2User.getAttributes()).willReturn(Map.of("email", "admin@gmail.com"));

		StringWriter stringWriter = getStringWriter();

		//when
		oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

		//then
		String responseBody = stringWriter.toString();
		Assertions.assertThat(responseBody).contains("signupToken");
		Assertions.assertThat(responseBody).contains("존재하지 않는 유저");
	}

	private StringWriter getStringWriter() throws IOException {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		given(response.getWriter()).willReturn(printWriter);
		return stringWriter;
	}

	@Test
	@DisplayName("google로 oauth 로그인시 DB에 저장된 email이라면, 메인 페이지로 redirect 시킨다.")
	void onAuthenticationSuccessWithProviderGoogleWithExistEmail() throws IOException {
		//given
		Member mockMember = Member.builder().email("admin@gmail.com").build();
		given(memberQueryService.findByEmail("admin@gmail.com")).willReturn(Optional.of(mockMember));

		given(authentication.getAuthorizedClientRegistrationId()).willReturn("google");
		given(authentication.getPrincipal()).willReturn(oAuth2User);
		given(oAuth2User.getAttributes()).willReturn(Map.of("email", "admin@gmail.com"));

		doNothing().when(tokenJpaRepository).deleteByMemberId(anyLong());
		given(tokenJpaRepository.save(any(Token.class))).willReturn(null);

		//when
		oAuth2SuccessHandler.onAuthenticationSuccess(AutoWiredRequest, AutoWiredResponse, authentication);

		//then
		Assertions.assertThat(AutoWiredResponse.getHeader("Authorization")).contains("Bearer");
		Assertions.assertThat(AutoWiredResponse.getHeader("location")).isNotBlank();

	}
}
