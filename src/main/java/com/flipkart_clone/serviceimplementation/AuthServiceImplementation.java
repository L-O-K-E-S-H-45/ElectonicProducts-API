package com.flipkart_clone.serviceimplementation;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flipkart_clone.cache.CacheStore;
import com.flipkart_clone.entities.AccessToken;
import com.flipkart_clone.entities.Customer;
import com.flipkart_clone.entities.RefreshToken;
import com.flipkart_clone.entities.Seller;
import com.flipkart_clone.entities.User;
import com.flipkart_clone.enums.UserRole;
import com.flipkart_clone.exception.IllegalRequestException;
import com.flipkart_clone.exception.InvalidOtpException;
import com.flipkart_clone.exception.OtpExpiredException;
import com.flipkart_clone.exception.UserAlreadyExistException;
import com.flipkart_clone.exception.UserExpiredException;
import com.flipkart_clone.exception.UserNotFoundByEmailException;
import com.flipkart_clone.exception.UserNotFoundException;
import com.flipkart_clone.exception.UserNotLoggedInException;
import com.flipkart_clone.repository.AccessTokenRepository;
import com.flipkart_clone.repository.CustomerRepository;
import com.flipkart_clone.repository.RefreshTokenRepository;
import com.flipkart_clone.repository.SellerRepository;
import com.flipkart_clone.repository.UserRepository;
import com.flipkart_clone.requestdtos.AuthRequest;
import com.flipkart_clone.requestdtos.OTPModdel;
import com.flipkart_clone.requestdtos.UserRequest;
import com.flipkart_clone.responsedtos.AuthResponse;
import com.flipkart_clone.responsedtos.UserResponse;
import com.flipkart_clone.security.Jwtservice;
import com.flipkart_clone.service.AuthService;
import com.flipkart_clone.util.CookieManager;
import com.flipkart_clone.util.MessageStructure;
import com.flipkart_clone.util.ResponseStructure;
import com.flipkart_clone.util.SimpleResponseStrusture;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
//@AllArgsConstructor
public class AuthServiceImplementation implements AuthService {

//	@Autowired // (this will do field injection, not good practice, better use constructor injection)
//	private UserRepository userRepo;
	
	private UserRepository userRepo;
	
	private SellerRepository sellerRepo;
	
	private CustomerRepository customerRepo;
	
	private PasswordEncoder passwordEncoder;
	
	private ResponseStructure<UserResponse> structure;
	private ResponseStructure<AuthResponse> authStructure;
	private SimpleResponseStrusture simpleResponseStrusture;
	
	private CacheStore<Integer> otpCacheStore;
	
	private CacheStore<User> userCacheStore;

	private JavaMailSender javaMailSender;
	
	private AuthenticationManager authenticationManager;
	
	private CookieManager cookieManager;
	
	private Jwtservice jwtservice;
	
	private AccessTokenRepository accessTokenRepo;
	
	private RefreshTokenRepository refreshTokenRepo;
	
	@Value("${myapp.access.expiry}")
	private int accessExpirationInseconds;
	
	@Value("${myapp.refresh.expiry}")
	private int refreshExpirationInseconds;
	
	public AuthServiceImplementation(UserRepository userRepo, 
			SellerRepository sellerRepo,
			CustomerRepository customerRepo, 
			PasswordEncoder passwordEncoder, 
			ResponseStructure<UserResponse> structure,
			ResponseStructure<AuthResponse> authStructure,
			SimpleResponseStrusture simpleResponseStrusture,
			CacheStore<Integer> otpCacheStore, 
			CacheStore<User> userCacheStore, 
			JavaMailSender javaMailSender,
			AuthenticationManager authenticationManager, 
			CookieManager cookieManager,
			Jwtservice jwtservice,
			AccessTokenRepository accessTokenRepo,
			RefreshTokenRepository refreshTokenRepo) {
		super();
		this.userRepo = userRepo;
		this.sellerRepo = sellerRepo;
		this.customerRepo = customerRepo;
		this.passwordEncoder=passwordEncoder;
		this.structure = structure;
		this.authStructure=authStructure;
		this.simpleResponseStrusture=simpleResponseStrusture;
		this.otpCacheStore = otpCacheStore;
		this.userCacheStore = userCacheStore;
		this.javaMailSender = javaMailSender;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtservice=jwtservice;
		this.accessTokenRepo=accessTokenRepo;
		this.refreshTokenRepo=refreshTokenRepo;
	}

	public <T extends User>T mapUserRequestToUserObject(UserRequest userRequest){
		User user=null;
		switch (userRequest.getUserRole()) {
		case CUSTOMER ->{user = new Customer();}
		case SELLER -> {user = new Seller();}
		}
		
//		user.setUserName(userRequest.getEmail().substring(0,userRequest.getEmail().indexOf('@')));
		user.setUserName(userRequest.getEmail().split("@")[0]);
		user.setEmail(userRequest.getEmail());
		user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		user.setUserRole(userRequest.getUserRole());
		
		return (T) user;
		
	}
	
	public UserResponse mapUserObjectToUserResponse(User user){
		return UserResponse.builder()
				.userId(user.getUserId())
				.userName(user.getUserName())
				.email(user.getEmail())
				.userRole(user.getUserRole())
				.build();
	}
	
	private User saveUser(UserRequest userRequest) {
		User user = mapUserRequestToUserObject(userRequest);
		switch (user.getUserRole()) {
		case CUSTOMER -> {customerRepo.save((Customer) user);}
		case SELLER -> {sellerRepo.save((Seller)user);}
		default -> throw new IllegalRequestException("Falied to register User b/z Invalid UserRole : "+user.getUserRole());
		}
		return user;
	}

//	@Scheduled(fixedDelay = 3000L)
	private int generateOTP() {
//		return (int) (Math.random() * 900000) + 100000;
		return new Random().nextInt(100000, 999999);
	}
	
	/**
	 * to make Asynchronous annotate with  @Async
	 * @method sendMail
	 */
	@Async
	private void sendMail(MessageStructure message) throws MessagingException {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
		
		helper.setTo(message.getTo());
		helper.setSubject(message.getSubject());
		helper.setSentDate(message.getSentDate());
		helper.setText(message.getText(),true);
		
		javaMailSender.send(mimeMessage);
		
	}

	private void sendOtpToMail(User user, int otp) throws MessagingException {

		sendMail(MessageStructure.builder()
		.to(user.getEmail())
		.subject("Complete your registration to FlipKart")
		.sentDate(new Date())
		.text(
				"Hey, <h2>"+user.getUserName() +"</h2> good to see you interested in flipkart"
				+"<br/>Complete your registration using the OTP <br/>"
				+"<h1>"+otp+"</h1><br/>"
				+"<h3>Note: OTP expires in 1 minute</h3>"
				+"<br/><br/>"
				+"<h3>with best regards<br/>"
				+"FlipKart</h3>"
				)
		.build());
	}
	
	@Async
	private void sendResponseMail(User user) throws MessagingException {
		sendMail(MessageStructure.builder()
		.to(user.getEmail())
		.subject("Registration to FlipKart successfull")
		.sentDate(new Date())
		.text(
				"Hey, <h2>"+user.getUserName()+"</h2> Welcome to FlipKart <br/>"
				+"Successfully completed registration to flipkart as a role : <h3>"+user.getUserRole()+"</h3>"
				+"<br/><br/>"
				+"<h3>with best regards<br/>"
				+"FlipKart</h3>"
				)
		.build());
	}
	
	@Override
	public ResponseEntity<ResponseStructure<String>> registerUser(UserRequest userRequest) {
		if (userRepo.existsByEmail(userRequest.getEmail()))
			throw new UserAlreadyExistException("User is already exists with the specified email id");
		
		int otp=generateOTP();
		
		User user = mapUserRequestToUserObject(userRequest);
		userCacheStore.add(user.getEmail(), user);
		otpCacheStore.add(user.getEmail(), otp);
		
		userRepo.save(user);
		
//		try {
//			sendOtpToMail(user, otp);
//		} catch (MessagingException e) {
//			throw new IllegalRequestException("Failed to send mail b/z "+e.getMessage());
//		}
		
		ResponseStructure<String> structure = new ResponseStructure<>();
		
		return new ResponseEntity<ResponseStructure<String>>(
				structure.setStatus(HttpStatus.ACCEPTED.value())
				.setMessage("Please verify your email by OTP sent to your email Id : ")
				.setData("Mail sent successfully to Verify Email"), HttpStatus.ACCEPTED);
		
	}
	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OTPModdel otpModdel) {
		User user = userCacheStore.get(otpModdel.getEmail());
		Integer otp = otpCacheStore.get(otpModdel.getEmail());
		
		if (otp!=null) {
			if (user!=null) {
				if (otp==otpModdel.getOtp()) {
					user.setEmailVerified(true);
					userRepo.save(user);
					try {
						sendResponseMail(user);
					} catch (MessagingException e) {
						throw new IllegalRequestException("Failed to send mail b/z "+e.getMessage());
					}
					return new ResponseEntity<ResponseStructure<UserResponse>>(
							structure.setStatus(HttpStatus.ACCEPTED.value())
							.setMessage(user.getUserName()+" Registered successfully as role: "+user.getUserRole())
							.setData(mapUserObjectToUserResponse(user)), HttpStatus.ACCEPTED);
				} else throw new InvalidOtpException("Please enter valid OTP");
			} else throw new UserExpiredException("Registration session expired");
		} else throw new OtpExpiredException("OTP expired!!!");
	}
	
	//---------------------------------------------------------------------
	
	private void grantAccess(HttpServletResponse response, User user) {
		// generating access & refresh tokens 
		String accessToken = jwtservice.generateAccessToken(user.getUserName());
		String refreshToken = jwtservice.generateAccessToken(user.getUserName());
		
		// adding access & refresh tokens to the response
		response.addCookie(cookieManager.configure(new Cookie("at", accessToken), accessExpirationInseconds));
		response.addCookie(cookieManager.configure(new Cookie("rt", refreshToken), refreshExpirationInseconds));
		
		// saving access & refresh cookie into the database
		accessTokenRepo.save(AccessToken.builder()
				.token(accessToken)
				.isBlocked(false)
				.user(user)
				.expiration(LocalDateTime.now().plusSeconds(accessExpirationInseconds))
				.build());
		
		refreshTokenRepo.save(RefreshToken.builder()
				.token(refreshToken)
				.isBlocked(false)
				.user(user)
				.expiration(LocalDateTime.now().plusSeconds(refreshExpirationInseconds))
				.build());
	}

	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest, HttpServletResponse response) {
		System.out.println("*******************************");
		String username = authRequest.getEmail().split("@")[0];
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken
				(username, authRequest.getPassword());
		Authentication authentication = authenticationManager.authenticate(token);
		if (!authentication.isAuthenticated())
			throw new UsernameNotFoundException("Failed to Authenticate user");
		else {
			// generating the cookies & authResponse & returning to the client
			return userRepo.findByUserName(username).map(user->{
				grantAccess(response, user);
				return ResponseEntity.ok(authStructure.setStatus(HttpStatus.OK.value())
					.setMessage("Login successfull")
					.setData(AuthResponse.builder()
							.userId(user.getUserId())
							.username(username)
							.role(user.getUserRole().name())
							.accessExpiration(LocalDateTime.now().plusSeconds(accessExpirationInseconds))
							.refreshExpiration(LocalDateTime.now().plusSeconds(refreshExpirationInseconds))
							.isAuthenticated(true)
							.build()));
			}).get();
		}
	}

	
	@Override
	public ResponseEntity<ResponseStructure<String>> traditionalLogout(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		
		for (Cookie cookie:cookies) {
			if (cookie.getName().equals("at")) {
				accessTokenRepo.findByToken(cookie.getValue()).ifPresent(accessToken->{
					accessToken.setBlocked(true);
					accessTokenRepo.save(accessToken);
				});
				response.addCookie(cookieManager.invalidate(new Cookie("at", "")));
			}
			if (cookie.getName().equals("rt")) {
				refreshTokenRepo.findByToken(cookie.getValue()).ifPresent(refreshToken->{
					refreshToken.setBlocked(true);
					refreshTokenRepo.save(refreshToken);
					});
				response.addCookie(cookieManager.invalidate(new Cookie("rt", "")));
			}
		}
		ResponseStructure<String> structure=new ResponseStructure<>();
		
		return ResponseEntity.ok(structure
				.setStatus(HttpStatus.OK.value())
				.setMessage("User successfully logged out")
				.setData("Logout successfull"));
			
	}
	
	
	@Override
	public ResponseEntity<SimpleResponseStrusture> logout(String accessToken, String refreshToken, HttpServletResponse response) {
		if (accessToken==null && refreshToken==null)
			throw new UserNotLoggedInException("User not logged in, Please login");
		accessTokenRepo.findByToken(accessToken).ifPresent(accesstoken->{
			accesstoken.setBlocked(true);
			accessTokenRepo.save(accesstoken);
			response.addCookie(cookieManager.invalidate(new Cookie("at", "")));
		});
		refreshTokenRepo.findByToken(accessToken).ifPresent(refreshtoken->{
			refreshtoken.setBlocked(true);
			refreshTokenRepo.save(refreshtoken);
			response.addCookie(cookieManager.invalidate(new Cookie("rt", "")));
		});
		
		SimpleResponseStrusture strusture=new SimpleResponseStrusture();
		return ResponseEntity.ok(strusture
				.setStatus(HttpStatus.OK.value())
				.setMessage("Logged out successfully"));
	}

	@Override
	public ResponseEntity<SimpleResponseStrusture> revokeAllDevices(String accessToken, String refreshToken,
			HttpServletResponse response) {
		if (accessToken==null || refreshToken==null)
			throw new UserNotLoggedInException("User not logged in, Please login b/z token is null");
		
		String username=SecurityContextHolder.getContext().getAuthentication().getName();
		if (username==null) throw new UserNotFoundException("User not logged in, Please login");
		
		userRepo.findByUserName(username).ifPresent(user->{
			blockAccessTokens(accessTokenRepo.findAllByUserAndIsBlocked(user,false));
			blockRefreshTokens(refreshTokenRepo.findAllByUserAndIsBlocked(user,false));
		});
		
		response.addCookie(cookieManager.invalidate(new Cookie("at", "")));
		response.addCookie(cookieManager.invalidate(new Cookie("rt", "")));
		
		return ResponseEntity.ok(simpleResponseStrusture
				.setStatus(HttpStatus.OK.value())
				.setMessage("Logged out from all devices"));
	}
	
	@Override
	public ResponseEntity<SimpleResponseStrusture> revokeOtherDevices(String accessToken, String refreshToken, HttpServletResponse response) {
		if (accessToken==null || refreshToken==null)
			throw new UserNotLoggedInException("User not logged in, Please login b/z token is null");
		
		String username=SecurityContextHolder.getContext().getAuthentication().getName();
		if (username==null) throw new UserNotFoundException("User not logged in, Please login");
		
		userRepo.findByUserName(username)
		.ifPresent(user->{
			System.out.println("accT: --- "+accessTokenRepo.findAllByUserAndIsBlockedAndTokenNot(user,false,accessToken));
			blockAccessTokens(accessTokenRepo.findAllByUserAndIsBlockedAndTokenNot(user,false,accessToken));
			blockRefreshTokens(refreshTokenRepo.findAllByUserAndIsBlockedAndTokenNot(user,false,refreshToken));
		});
		
		response.addCookie(cookieManager.invalidate(new Cookie("at", "")));
		response.addCookie(cookieManager.invalidate(new Cookie("rt", "")));
		
		return ResponseEntity.ok(simpleResponseStrusture
				.setStatus(HttpStatus.OK.value())
				.setMessage("Access revoked to other devices"));
	}
	
	//---------------------------------------
	private void blockAccessTokens(List<AccessToken> accessTokens) {
		
		accessTokens.forEach(token->{
			token.setBlocked(true);
			accessTokenRepo.save(token);
		});
	}
	private void blockRefreshTokens(List<RefreshToken> refreshTokens) {
		refreshTokens.forEach(token->{
			token.setBlocked(true);
			refreshTokenRepo.save(token);
		});
	}
	

	// -----------------------------
	public void cleanupUnverifiedUsers() {
		System.out.println("STARTS -> cleanupUnverifiedUsers()");
		userRepo.deleteAll(userRepo.findByIsEmailVerifiedFalse());
		System.out.println("ENDS -> cleanupUnverifiedUsers()");
	}

	public void cleanupExpiredAccessTokens() {
		System.out.println("STARTS -> cleanupExpiredAccessTokens()");
		accessTokenRepo.deleteAll(accessTokenRepo.findAllByExpirationBefore(LocalDateTime.now()));
		System.out.println("ENDS -> cleanupExpiredAccessTokens()");
	}
	
	public void cleanupExpiredRefreshTokens() {
		System.out.println("STARTS -> cleanupExpiredRefreshTokens()");
		refreshTokenRepo.deleteAll(refreshTokenRepo.findAllByExpirationBefore(LocalDateTime.now()));
		System.out.println("ENDS -> cleanupExpiredRefreshTokens()");
			
	}


}










