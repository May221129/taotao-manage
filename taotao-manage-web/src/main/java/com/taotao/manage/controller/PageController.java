package com.taotao.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 通用页面跳转逻辑
 */
@Controller
@RequestMapping("page")
public class PageController extends BaseController {
	
	/**
	 * 具体的跳转页面逻辑：
	 * @param pageName
	 * @return 视图名
	 */
	@RequestMapping(value="{pageName}", method = RequestMethod.GET)
	public String toPage(@PathVariable("pageName") String pageName){
		return pageName;
	}
}
