package pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@TableName("hq_member")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class member extends BasePojo{
	@TableId(type=IdType.AUTO)
	
  private Integer id;
  private String name;
  private String password;
  private String gender;
  private String birthday;
  private String phone;
  private String career;
  private String is_member;
  private String level;
  
}
