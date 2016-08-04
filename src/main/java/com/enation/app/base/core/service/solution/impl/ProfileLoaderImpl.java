package com.enation.app.base.core.service.solution.impl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.enation.framework.util.FileUtil;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.enation.app.base.core.service.solution.IProfileLoader;
import com.enation.eop.sdk.context.EopSetting;

import java.io.InputStream;

public class ProfileLoaderImpl implements IProfileLoader {

	protected final Logger logger = Logger.getLogger(getClass());

	/**
	 * 加载产品的profile文件
	 * @param productId 产品标识，同时在resources目录对应文件夹名称
	 *                  如company 对应 resources目录 db_init/products/company
	 * @return
	 */
	public Document load(String productId) {
		//String xmlFile = EopSetting.PRODUCTS_STORAGE_PATH+"/"+productId +"/profile.xml";
		InputStream is = FileUtil.getResourceAsStream("db_init/products/" + productId + "/profile.xml");

		try {
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    //Document document = builder.parse(xmlFile);
			Document document = builder.parse(is);
		    return document;
		}
		catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new RuntimeException("load ["+productId +"] profile error" );
		} 	 
		 
	}

}
