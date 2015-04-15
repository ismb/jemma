package org.energy_home.jemma.ah.demo.fakevalues;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeValuesComponent extends HttpServlet implements ManagedService {
	
	private final static Logger log=LoggerFactory.getLogger(FakeValuesComponent.class);
	private final static String fakeDataServletAlias="/fakevalues";
	
	
	private ConfigurationAdmin configurationAdmin;
	private HttpService httpService;
	private ComponentContext componentContext;

	private Dictionary<String, ?> props;
	
	public void activate(ComponentContext context)
	{
		this.componentContext=context;
		
		Dictionary serviceProps=new Hashtable();
		serviceProps.put(Constants.SERVICE_PID,getServicePID());
		this.componentContext.getBundleContext().registerService(ManagedService.class.getName(), this, serviceProps);
		
		try {
			httpService.registerServlet(fakeDataServletAlias, this, null, null);
		} catch (Exception e) {	
			log.error("Unable to register servlet for fake values",e);
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		
		 response.setContentType("application/json");
		
		String responseString=null;
		if(props==null)
		{
			responseString="{}";
		}else{
			 responseString="{";
			 for (Enumeration e = props.keys(); e.hasMoreElements();) 
			 {
				 String key=(String)e.nextElement();
				 String value=(String) props.get(key);
				 responseString+="\""+key+"\":\""+value+"\"";
				 if(e.hasMoreElements())
				 {
					 responseString+=",";
				 }
			 }
			 responseString+="}";
		}
		
		try {
			response.getWriter().write(responseString);
			response.getWriter().flush();
		} catch (IOException e) {
			log.error("Error sending response to the client");
		}
		
				
	}
	
	
	private String getServicePID() {
		return  "jemma.osgi.ah.fakevalues";
	}

	public void bindConfigurationAdmin(ConfigurationAdmin configurationAdmin){
		this.configurationAdmin=configurationAdmin;
	}
	
	public void unbindConfigurationAdmin(ConfigurationAdmin configurationAdmin){
		this.configurationAdmin=null;
	}
	
	public void bindHttpService(HttpService httpService)
	{
		this.httpService=httpService;
	}
	
	
	public void unbindHttpService(HttpService httpService)
	{
		this.httpService=httpService;
	}
	
	
	@Override
	public void updated(Dictionary<String, ?> conf) throws ConfigurationException {
		
		if (conf == null){
			// Configuration not present, load from file in bundle
			Dictionary defaultProps;
			try {
				defaultProps = loadPropFile();
			
				configurationAdmin.getConfiguration(getServicePID()).update(defaultProps);
			} catch (IOException e) {
				log.error("Unable to load  noserver.properties file");
			}
		}else{
			//save locally properties
			this.props=conf;
		}
	}
	
	
	private Dictionary loadPropFile() throws IOException {

		// String _path = "noserver.properties";
		//String _path = System.getProperty("user.home") + File.separator + "noserver.properties";
		String _path_recovery = "noserver.properties";
		BundleContext bc = this.componentContext.getBundleContext();
			
		URL _url = bc.getBundle().getResource(_path_recovery);

		Properties readProps=new Properties();
 		InputStream stream = _url.openStream();
		readProps.load(stream);
		stream.close();
			
		return readProps;

	}
	


}
