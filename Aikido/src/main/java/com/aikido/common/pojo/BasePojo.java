package com.aikido.common.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.experimental.Accessors;

//pojo���࣬���2������2�����ڣ�ʵ�����л�
@Data	//set/get����
@Accessors(chain=true)
public class BasePojo implements Serializable{
	private Date created_time;	
	private Date updated_time;

}
