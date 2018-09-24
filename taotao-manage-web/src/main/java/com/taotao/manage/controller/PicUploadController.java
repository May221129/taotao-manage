package com.taotao.manage.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.PicUploadResult;
import com.taotao.manage.service.PropertiesService;

/**
 * 图片上传Controller
 * 	1.通过Apache提供的commons-fileupload上传组件完成文件上传的具体步骤：
 * 	  https://www.cnblogs.com/com-itheima-crazyStone/p/6739862.html
 * 	2.上传到Spring MVC程序中的文件会被封装到一个MultipartFile对象中，所以和不借助SpringMVC框架，而是自己写文件上传不同，这里不会用到FileItem。
 */
@Controller
@RequestMapping("pic")
public class PicUploadController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PicUploadController.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	// 允许上传的格式
	private static final String[] IMAGE_TYPE = new String[] { ".bmp", ".jpg", ".jpeg", ".gif", ".png" };
	
	/**
	 * 读取environment.properties配置文件中的内容，来替换该Controller中写死的地址和url：
	 * 	1.@Value作用：获取配置文件的值。
	 * 		注入值：在Spring容器初始化之后（即初始化完所有的bean），@value的特性是只在当前的所在容器中获取值，然后注入。
	 * 		提问①：这个资源文件是springmvc子容器所需的，按理说直接在子容器的配置文件中配置管理即可，在spring父容器中可以不用配置。
				结果：在springmvc子容器中必须配置，且在springmvc父容器中也必须配置，否则报Illegal	ArgumentException
			提问②：这里为什么又注掉了@Value?
			答：为了不在spring父容器和springmvc子容器这两个容器的配置文件中都去配置environment.properties资源文件的bean管理，
				所以改用将environment.properties资源文件作为service交给spring来管理，在controller层进行自动注入的方式来代替。
	 * 	2.面试题：这里涉及到spring容器和springmvc容器之间的关系：
	 *		见：Q:\\mystudy\\studynotes\\面试题（一）
	 */
//	@Value("${REPOSITORY_PATH}")
//	private String REPOSITORY_PATH;
//	
//	@Value("${IMAGE_BASE_URL}")
//	private String IMAGE_BASE_URL;
	
	@Autowired
	public PropertiesService propertiesService;
	
	/**
	 * produces:RequestMapping的一个属性，用于指定响应类型。
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String upload(@RequestParam("uploadFile") MultipartFile uploadFile , HttpServletResponse response) throws Exception {

		// 校验图片格式 
		boolean isLegal = false;
		for (String type : IMAGE_TYPE) {
			if (StringUtils.endsWithIgnoreCase(uploadFile.getOriginalFilename(), type)) {
				isLegal = true;
				break;
			}
		}

		// 封装Result对象，并且将文件的byte数组放置到result对象中
		PicUploadResult fileUploadResult = new PicUploadResult();

		// 状态
		fileUploadResult.setError(isLegal ? 0 : 1);

		// 文件新路径
		String filePath = getFilePath(uploadFile.getOriginalFilename());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Pic file upload .[{}] to [{}] .", uploadFile.getOriginalFilename(), filePath);
		}

		// 生成图片的绝对引用地址
		String picUrl = StringUtils.replace(StringUtils.substringAfter(filePath, propertiesService.REPOSITORY_PATH), "\\", "/");
		fileUploadResult.setUrl(propertiesService.IMAGE_BASE_URL + picUrl);//图片的访问地址

		File newFile = new File(filePath);

		// 写文件到磁盘
		uploadFile.transferTo(newFile);

		// 校验图片是否合法
		isLegal = false;
		try {
			BufferedImage image = ImageIO.read(newFile);
			if (image != null) {
				fileUploadResult.setWidth(image.getWidth() + "");
				fileUploadResult.setHeight(image.getHeight() + "");
				isLegal = true;
			}
		} catch (IOException e) {
		}

		// 状态
		fileUploadResult.setError(isLegal ? 0 : 1);

		if (!isLegal) {
			// 不合法，将磁盘上的文件删除
			newFile.delete();
		}
		
		/**
		 * ObjectMapper mapper这个类是Jackson提供的类。
		 * writeValueAsString()：将对象序列化为json字符串。
		 */
		return mapper.writeValueAsString(fileUploadResult);
	}
	
	/**
	 * 该方法的作用：
	 * 		会在"Q:\\taotaoUpload"目录下，创建一个"images"文件夹，
	 * 		该文件夹中，会以年、月、日来逐级创建目录来存储图片
	 */
	private String getFilePath(String sourceFileName) {
		String baseFolder = propertiesService.REPOSITORY_PATH + File.separator + "images";
		Date nowDate = new Date();
		// yyyy/MM/dd
		String fileFolder = baseFolder + File.separator + new DateTime(nowDate).toString("yyyy") + File.separator + new DateTime(nowDate).toString("MM") + File.separator
				+ new DateTime(nowDate).toString("dd");
		File file = new File(fileFolder);
		if (!file.isDirectory()) {
			// 如果目录不存在，则创建目录
			file.mkdirs();
		}
		// 生成新的文件名
		String fileName = new DateTime(nowDate).toString("yyyyMMddhhmmssSSSS") + RandomUtils.nextInt(100, 9999) + "." + StringUtils.substringAfterLast(sourceFileName, ".");
		return fileFolder + File.separator + fileName;
	}
}
