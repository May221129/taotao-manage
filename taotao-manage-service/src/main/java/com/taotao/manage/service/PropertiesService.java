package com.taotao.manage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 专门用于读取资源文件的service
 */
@Service
public class PropertiesService {
	
	@Value("${REPOSITORY_PATH}")
	public String REPOSITORY_PATH;
	
	@Value("${IMAGE_BASE_URL}")
	public String IMAGE_BASE_URL;
}
