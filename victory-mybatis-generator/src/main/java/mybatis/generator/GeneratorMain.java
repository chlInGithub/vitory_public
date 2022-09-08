package mybatis.generator;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取 MBG 配置生成代码
 *
 * @author hailongchen9
 */
public class GeneratorMain {

	static  void generating(String generatorConfigFileName) throws Exception{
		//MBG 执行过程中的警告信息
		List<String> warnings = new ArrayList<>();
		//当生成的代码重复时，覆盖原代码
		boolean overwrite = true;
		//读取我们的 MBG 配置文件
		InputStream is = GeneratorMain.class.getResourceAsStream("/mybatis/generator/" + generatorConfigFileName);
		ConfigurationParser cp = new ConfigurationParser(warnings);
		Configuration config = cp.parseConfiguration(is);
		is.close();

		DefaultShellCallback callback = new DefaultShellCallback(overwrite);
		//创建 MBG
		MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
		//执行生成代码
		myBatisGenerator.generate(null);
		//输出警告信息
		for(String warning : warnings){
			System.out.println(warning);
		}
	}
	public static void main(String[] args){
		String[] fileNames = new String[]{
				"generatorConfig-sale-strategy.xml"
				/*,"generatorConfig-shop-power.xml"
				,"generatorConfig-fee-rule.xml"
				,"generatorConfig-order-fee.xml"
				,"generatorConfig-shop-app.xml"
				,"generatorConfig-access-limit.xml"
				,"generatorConfig-shop-img.xml"
				,"generatorConfig-shop-img-total.xml"
				,"generatorConfig-user-coupons.xml"
				,"generatorConfig-activity.xml"
				,"generatorConfig-activity-query.xml"
				,"generatorConfig-coupons.xml"
				,"generatorConfig-coupons-query.xml"
				,"generatorConfig-item.xml"
				,"generatorConfig-item-query.xml"
				,"generatorConfig-member.xml"
				,"generatorConfig-member-query.xml"
				,"generatorConfig-merchant.xml"
				,"generatorConfig-merchant-query.xml"
				,"generatorConfig-order.xml"
				,"generatorConfig-order-query.xml"
				,"generatorConfig-sms.xml"
				,"generatorConfig-sms-query.xml"*/

				/*"generatorConfig-activity.xml"
				,"generatorConfig-coupons.xml"
				,"generatorConfig-item.xml"
				,"generatorConfig-member.xml"
				,"generatorConfig-merchant.xml"
				,"generatorConfig-order.xml"
				,"generatorConfig-sms.xml"*/

				/*"generatorConfig-activity-query.xml"
				,"generatorConfig-coupons-query.xml"
				,"generatorConfig-item-query.xml"
				,"generatorConfig-member-query.xml"
				,"generatorConfig-merchant-query.xml"
				,"generatorConfig-order-query.xml"
				,"generatorConfig-sms-query.xml"*/
		};
		for (String fileName : fileNames) {
			try {
				generating(fileName);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(fileName + " 	ex");
			}
		}
	}
}
