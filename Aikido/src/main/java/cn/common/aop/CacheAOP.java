package cn.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.jt.util.ObjectMapperUtil;

import cn.common.annotation.CacheFind;
import redis.clients.jedis.JedisCluster;

@Component	//灏嗗璞′氦缁檚pring瀹瑰櫒绠＄悊
@Aspect		//鑷畾涔夊垏闈�
public class CacheAOP {


	/**
	 * 1.娣诲姞鐜粫閫氱�?  浣跨敤鐜粫閫氱煡蹇呴�?�娣诲姞杩斿洖鍊�?
	 * 2.鎷︽埅鑷畾涔夋敞瑙�?
	 *   闇�姹�: 鍔ㄦ�佽幏鍙栬嚜瀹氫箟娉ㄨВ涓殑鍙傛�?
	 * 宸ヤ綔鍘熺悊璇存�?:
	 * 	瀹氫箟娉ㄨВ鐨勫彉閲忓悕绉�?  cacheFind
	 *      閫氱煡鍙傛暟鎺ユ敹鍚嶇�?     cacheFind
	 *      闄や簡鍖归厤鍚嶇О涔嬪�?,杩橀渶瑕佸尮閰嶇被鍨�?.
	 *      
	 * 娉ㄦ剰浜嬮�??: 
	 * 	1.鐜粫閫氱煡浣跨敤鏃�?,蹇呴』娣诲姞ProceedingJoinPoint
	 *  2.骞朵笖鍏朵腑鐨勫弬鏁癹oinPoint,蹇呴』浣嶄簬绗竴浣�?.
	 *  
	 * 缂撳瓨�?�炵幇涓氬姟鎬濊�?:
	 * 	1.鍑嗗key   1:鍔ㄦ�佺殑鐢熸垚key    2:鐢ㄦ埛鎸囧畾鐨刱ey    key鏄惁鏈夊��?.
	 * 	2.鍏堟煡璇㈢紦瀛�
	 * 		2.1 娌℃湁鏁版嵁   鎵ц鏁版嵁搴撴搷浣�. 鎵ц鐩爣鏂规硶
	 * 		灏嗙洰鏍囨柟娉曠殑杩斿洖鍊� 杞寲涓篔SON涓�,淇濆瓨鍒皉edis涓�.
	 * 		2.2 鏈夋暟鎹�?      鍔ㄦ�佽幏鍙栫紦瀛樻暟鎹箣鍚庡埄鐢ㄥ伐鍏稟PI杞寲涓虹湡瀹炵殑�?�硅�?.
	 */

	//@Autowired
	//private Jedis jedis;		//鍗曞彴redis
	//@Autowired					
	//private ShardedJedis jedis;//閰嶇疆鍒嗙墖鏈哄�?
	
	//@Autowired //1.鎸夌収绫诲�?�杩涜娉ㄥ�?   2.鎸夌収鍚嶇О鍖归厤
	//@Qualifier("sentinelJedis") //鎸囧畾bean鐨勫悕绉拌繘琛屾敞鍏�?
	//private Jedis jedis;       //浠庡摠鍏典腑鑾峰彇鐨刯edis
	
	@Autowired
	private JedisCluster jedis;
	

	@Around("@annotation(cacheFind)")
	public Object around(ProceedingJoinPoint joinPoint,CacheFind cacheFind) {
		//璋冪敤鏂规硶,鑾峰彇key
		String key = getKey(joinPoint,cacheFind);
		String value = jedis.get(key);
		Object object = null;
		try {
			if(StringUtils.isEmpty(value)) {
				//缂撳瓨涓病鏈夋暟鎹�?,鏌ヨ鏁版嵁搴�.
				object = joinPoint.proceed();
				//灏嗘暟鎹繚瀛樺埌redis涓�
				String json = ObjectMapperUtil.toJSON(object);
				//鍒ゆ柇鏄惁闇�瑕佽秴鏃惰瀹�
				if(cacheFind.secondes()>0) {
					int seconds = cacheFind.secondes();
					jedis.setex(key,seconds,json);
				}else {
					//璇ユ暟鎹案涓嶈秴鏃�?
					jedis.set(key, json);
				}
				System.out.println("AOP鏌ヨ鏁版嵁搴�!!!!!");
			}else {
				//琛ㄧずvalue鍊间笉涓簄ull,璧扮紦�?�樻搷浣�?.
				Class<?> targetClass = getReturnType(joinPoint);
				object = ObjectMapperUtil.toObj(value, targetClass);
				System.out.println("AOP鏌ヨ缂撳瓨!!!!");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		
		//灏唈edis閾炬帴鍏抽棴,鐢ㄦ埛闅忕敤闅忓�?
		//jedis.close();
		return object;
	}

	//闇�姹傚姩鎬佺殑鑾峰彇杩斿洖鍊肩被鍨�?
	private Class<?> getReturnType(ProceedingJoinPoint joinPoint) {
		//Signature鏍囪瘑鏂规硶鐨凙PI  鍒╃敤鍙嶅皠鍔ㄦ�佽幏鍙杕ethod瀵硅�? 鑾峰彇杩斿洖鍊�
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		return methodSignature.getReturnType();
	}

	//鍔ㄦ�佽幏鍙杒ey
	private String getKey(ProceedingJoinPoint joinPoint, CacheFind cacheFind) {
		//1.妫�鏌ョ敤鎴锋槸鍚︿紶閫択ey
		String key = cacheFind.key();
		if(StringUtils.isEmpty(key)) {
			//鍖呭�?.绫诲�?.鏂规硶鍚�?::绗竴涓弬鏁�
			String className = joinPoint.getSignature().getDeclaringTypeName();
			String methodName = joinPoint.getSignature().getName();
			Object arg0 = joinPoint.getArgs()[0];
			key = className+"."+methodName+"::"+arg0;
		}
		return key;
	}



	//鍒囬�? = 鍒囧叆鐐硅�?�杈惧紡 + 閫氱�?

	//闇�姹�1:鍓嶇疆閫氱煡 鎷︽埅鎵�鏈夌殑service灞傜殑鏂规硶.
	//瀹氫箟鍏�?叡鐨勫垏鍏ョ偣琛ㄨ揪寮�
	/*@Pointcut("execution(* com.jt.service..*.*(..))")
	public void pointCut() {

	}	*/

	//鑾峰彇鐩爣瀵硅薄鐨勬柟娉曞悕绉�?.
	//@Before("pointCut()")  //閫氱煡鍏宠仈鍒囧叆鐐硅�?�杈惧紡4
	/*@Before("execution(* com.jt.service..*.*(..))")
	public void before(JoinPoint joinPoint) {
		String className = joinPoint.getSignature().getDeclaringTypeName();
		String methodName = joinPoint.getSignature().getName();
		System.out.println(methodName);
	}*/
}
