package com.nt.bindings;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
	
	private String name;
	private String email;
	private Long mobileNo;
	private String gender="male";
	private LocalDate dob = LocalDate.now();
	private Long aadharNo;
	public Integer getUserId() {
		// TODO Auto-generated method stub
		return null;
	}

}
