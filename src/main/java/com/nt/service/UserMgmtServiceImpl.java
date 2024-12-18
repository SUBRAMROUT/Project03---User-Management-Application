package com.nt.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.nt.bindings.ActivateUser;
import com.nt.bindings.LoginCredentials;
import com.nt.bindings.RecoverPassword;
import com.nt.bindings.UserAccount;
import com.nt.entity.UserMaster;
import com.nt.repository.IUserMasterRepository;
import com.nt.utils.EmailUtils;

@Service
public class UserMgmtServiceImpl implements IUserMgmtService {
	@Autowired
	private IUserMasterRepository userMasterRepo;
	@Autowired
	private EmailUtils emailUtils;
	@Autowired
	private Environment env;

	@Override
	public String registerUser(UserAccount user)throws Exception {
		//convert UserAccount obj data to UserMaster obj (Entity obj) data
		UserMaster master = new UserMaster();
		BeanUtils.copyProperties(user, master);
		//set random string of 6 chars as password
		String tempPwd=generateRandomPassword(6);
		master.setPassword(tempPwd);
		master.setActive_SW("InActive");
		//save object
		UserMaster savedMaster = userMasterRepo.save(master);
		//TODO: send the mail
		String subject ="User Registration Success";
		String body=readEmailMessageBody(env.getProperty("mailbody.registeruser.location"), user.getName(),tempPwd);
		emailUtils.sendEmailMessage(user.getEmail(),subject,body);
		//return message
		return savedMaster!=null?"User is registered with Id value::"+savedMaster.getUserId():"problem in user registration";
	}
	
    //helper method for same class
	private String generateRandomPassword(int length) {
		//a list of characters to choose from in form of a string
		String AlphaNumericStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		//creating a stringBuffer size of AlphanumericStr
		StringBuilder randomWord = new StringBuilder(length);
		int i;
		for (i=0;i<length;i++) {
			//generate a random number using math.random() (gives psuedo random number 0.0 to 1.0)
			int ch =(int)(AlphaNumericStr.length()*Math.random());
			//adding Random character one by one at the end of randomword
			randomWord.append(AlphaNumericStr.charAt(ch));
		}
		return randomWord.toString();
	}

	@Override
	public String activateUserAccount(ActivateUser user) {
		//use findBy method
		UserMaster entity=userMasterRepo.findByEmailAndPassword(user.getEmail(), user.getTempPassword());
		if(entity==null) {
			return "User is not found for activation";
		}
		else {
			//set the password
			entity.setPassword(user.getConfirmPassword());
			//change the user account status to active 
			entity.setActive_SW("Active");
			//update the obj
			UserMaster updatedEntity =userMasterRepo.save(entity);
			return "User is activated with new Password";
		}
	}

	@Override
	public String login(LoginCredentials credentials) {
		//convert LoginCredentials object to UserMaster object(Entity object)
		UserMaster master= new UserMaster();
		BeanUtils.copyProperties(credentials, master);
		//prepare example obj
		Example<UserMaster> example=Example.of(master);
		List<UserMaster> listEntities=userMasterRepo.findAll(example);
		if(listEntities.size()==0) {
			return "Invalid Credentials";
		}
		else {
			//get entity obj
			UserMaster entity=listEntities.get(0);
			if(entity.getActive_SW().equalsIgnoreCase("Active")) {
				return "Valid Credentials and login successful";
			}
			else {
				return "User Account is not active";
			}
		}
		
	}

	@Override
	public List<UserAccount> listUsers() {
		// load all entities and convert to UserAccount obj
		List<UserAccount> listUsers =userMasterRepo.findAll().stream().map(entity->{
			UserAccount user = new UserAccount();
			BeanUtils.copyProperties(entity, user);
			return user;
		}).toList();
		return listUsers;
		
		//load all entities and convert to UserAccount obj
		          //convert all entities to UserAccount obj
		/*List<UserAccount> listEntities=userMasterRepo.findAll();
		  List<UserAccount> listUsers=new ArrayList();
		  listEntities.forEach(entities->{
			  UserAccount user=new UserAccount();
			  BeanUtils.copyProperties(entities, user);
			  listUsers.add(user);
		  });
		  return listUsers;*/
		  
		
	}

	@Override
	public UserAccount showUserByUserId(Integer id) {
		//Load the user by user id
		Optional<UserMaster> opt=userMasterRepo.findById(id);
		UserAccount account = null;
		if(opt.isPresent()) {
			account=new UserAccount();
			BeanUtils.copyProperties(opt.get(), account);
		}
		return account;
	}

	@Override
	public UserAccount ShowUserByEmailAndName(String email, String name) {
		//use the custom findBy(-)method
		UserMaster master=userMasterRepo.findByNameAndEmail(name, email);
		UserAccount account=null;
		if(master!=null) {
			account=new UserAccount();
			BeanUtils.copyProperties(master, account);
		}
		return account;
	}

	@Override
	public String updateUser(UserAccount user) {
		// use the custom findBy(-) method
		Optional<UserMaster> opt=userMasterRepo.findById(user.getUserId());
		if(opt.isPresent()) {
			//get Entity object
			UserMaster master=opt.get();
			BeanUtils.copyProperties(user, master);
			userMasterRepo.save(master);
			return "User Details are updated";
		}
		else {
			return "User not found for updation";
		}
		
	}

	@Override
	public String deleteUserById(Integer id) {
		//Load the obj
		Optional<UserMaster> opt=userMasterRepo.findById(id);
		if(opt.isPresent()) {
			userMasterRepo.deleteById(id);
			return "User is deleted";
			
		}
		return "user is not found for deletion";
	}

	@Override
	public String changeUserStatus(Integer id, String status) {
		// Load the obj
		Optional<UserMaster> opt=userMasterRepo.findById(id);
		if(opt.isPresent()) {
			//get Entity obj
			UserMaster master=opt.get();
			//change the status
			master.setActive_SW(status);
			//update the obj
			userMasterRepo.save(master);
			return "User status changed";
		}
		return "user not found for changing the status";
	}

	@Override
	public String recoverPassword(RecoverPassword recover)throws Exception {
		// get UserMaster (Entity obj) by name,email
		UserMaster master=userMasterRepo.findByNameAndEmail(recover.getName(), recover.getEmail());
		if(master!=null) {
			String pwd=master.getPassword();
			//TODO:sent the recovered ti email account
			String subject="mail for password recovery";
			String mailBody=readEmailMessageBody(env.getProperty("mailbody.recoverpwd.location"), recover.getName(),pwd);
			emailUtils.sendEmailMessage(recover.getEmail(),subject, mailBody);
			return pwd;
		}
		return "User and email is not found";
	}
	
	//to read mail message body from file
	private String readEmailMessageBody(String fileName,String fullName,String pwd)throws Exception{
		String mailBody=null;
		String url="";
		try(FileReader reader=new FileReader(fileName);
				BufferedReader br=new BufferedReader(reader)){
			//read file content to StringBuffer object line by line
			StringBuffer buffer=new StringBuffer();
			String line=null;
			do {
				line=br.readLine();
				buffer.append(line);
			}while(line!=null);
			
			mailBody=buffer.toString();
			mailBody.replace("{FULL-NAME}", fullName);
			mailBody.replace("{PWD}", pwd);
			mailBody.replace("{URL}", url);
			
		}//try
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		return mailBody;
		
	}

}
