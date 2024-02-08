package com.flipkart_clone.serviceimplementation;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.flipkart_clone.cache.CacheStore;
import com.flipkart_clone.entities.Customer;
import com.flipkart_clone.entities.Seller;
import com.flipkart_clone.entities.User;
import com.flipkart_clone.enums.UserRole;
import com.flipkart_clone.exception.IllegalRequestException;
import com.flipkart_clone.exception.InvalidOtpException;
import com.flipkart_clone.exception.OtpExpiredException;
import com.flipkart_clone.exception.UserAlreadyExistException;
import com.flipkart_clone.exception.UserExpiredException;
import com.flipkart_clone.exception.UserNotFoundByEmailException;
import com.flipkart_clone.repository.CustomerRepository;
import com.flipkart_clone.repository.SellerRepository;
import com.flipkart_clone.repository.UserRepository;
import com.flipkart_clone.requestdtos.OTPModdel;
import com.flipkart_clone.requestdtos.UserRequest;
import com.flipkart_clone.responsedtos.UserResponse;
import com.flipkart_clone.service.AuthService;
import com.flipkart_clone.util.MessageStructure;
import com.flipkart_clone.util.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImplementation implements AuthService {

//	@Autowired // (this will do field injection, not good practice, better use constructor injection)
//	private UserRepository userRepo;
	
	private UserRepository userRepo;
	
	private SellerRepository sellerRepo;
	
	private CustomerRepository customerRepo;
	
	private ResponseStructure<UserResponse> structure;
	
	private CacheStore<Integer> otpCacheStore;
	
	private CacheStore<User> userCacheStore;

	private JavaMailSender javaMailSender;
	
	public <T extends User>T mapUserRequestToUserObject(UserRequest userRequest){
		User user=null;
		switch (userRequest.getUserRole()) {
		case CUSTOMER ->{user = new Customer();}
		case SELLER -> {user = new Seller();}
		}
		
//		user.setUserName(userRequest.getEmail().substring(0,userRequest.getEmail().indexOf('@')));
		user.setUserName(userRequest.getEmail().split("@")[0]);
		user.setEmail(userRequest.getEmail());
		user.setPassword(userRequest.getPassword());
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
		return (int) (Math.random() * 900000) + 100000;
	}
	
	/**
	 * to make Asynchronous annotate with  @Async
	 * @param messageStructure
	 */
	@Async
	private void sendMail(MessageStructure message) throws MessagingException {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
		
		helper.setTo(message.getTo());
		helper.setSubject(message.getSubject());
		helper.setSentDate(message.getSentDate());
		helper.setText(message.getText());
		
		javaMailSender.send(mimeMessage);
		
	}
	
	private void sendOtpToMail(User user, int otp) throws MessagingException {

		sendMail(MessageStructure.builder()
		.to(user.getEmail())
		.subject("Complete your registration to FlipKart")
		.sentDate(new Date())
		.text(
				"Hey, "+user.getUserName() +" good to see you interested in flipkart, "
				+"Complete your registration using the OTP <br/>"
				+"<h1>"+otp+"</h1><br/>"
				+"Note: OTP expires in 1 minute"
				+"<br/><br/>"
				+"with best regards<br/>"
				+"FlipKart"
				)
		.build());
	}
	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {
		if (userRepo.existsByEmail(userRequest.getEmail()))
			throw new UserAlreadyExistException("");
		
		int otp=generateOTP();
		System.out.println("OTP -> "+otp);
		
		User user = mapUserRequestToUserObject(userRequest);
		userCacheStore.add(user.getEmail(), user);
		otpCacheStore.add(user.getEmail(), otp);
		
		try {
			sendOtpToMail(user, otp);
		} catch (MessagingException e) {
//			throw new IllegalRequestException("Failed to send mail!!!");
			throw new IllegalRequestException(e.getMessage());
		}
		
		return new ResponseEntity<ResponseStructure<UserResponse>>(
				structure.setStatus(HttpStatus.ACCEPTED.value())
				.setMessage("kindly verify your email by "+ "OTP sent to your email Id : ")
				.setData(mapUserObjectToUserResponse(user)), HttpStatus.ACCEPTED);
		
	}
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OTPModdel otpModdel) {
//		String existOTP = otpCacheStore.get("key");
//		if (existOTP!=null) { 
//			if (existOTP.equals(otp)) return new ResponseEntity<String>(existOTP,HttpStatus.OK);
//			else return new ResponseEntity<String>("OTP mismatch",HttpStatus.OK);
//		}else return new ResponseEntity<String>("OTP is Expired",HttpStatus.OK);
		
		User user = userCacheStore.get(otpModdel.getEmail());
		int otp = otpCacheStore.get(otpModdel.getEmail());
		
		if (otp!=0) {
			if (user!=null) {
				if (otp==otpModdel.getOtp()) {
					user.setEmailVerified(true);
					userRepo.save(user);
					return new ResponseEntity<ResponseStructure<UserResponse>>(
							structure.setStatus(HttpStatus.ACCEPTED.value())
							.setMessage(user.getUserName()+" Registered successfully with "+user.getUserRole())
							.setData(mapUserObjectToUserResponse(user)), HttpStatus.ACCEPTED);
				} else throw new InvalidOtpException("Invalid OTP");
			} else throw new UserExpiredException("Registration session expired");
		} else throw new OtpExpiredException("OTP expired!!!");
	}



	

	
	
	
	public void cleanupUnverifiedUsers() {
//		List<User> users = userRepo.findByIsEmailVerifiedFalse();
//		userRepo.deleteAll(users);
		
		userRepo.deleteAll(userRepo.findByIsEmailVerifiedFalse());
	}


}










