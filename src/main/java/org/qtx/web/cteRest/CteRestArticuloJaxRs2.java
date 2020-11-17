package org.qtx.web.cteRest;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import org.qtx.dominio.Articulo;
import org.qtx.web.cte.CteException;
import org.qtx.web.cte.ICteRestArticulo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Component
public class CteRestArticuloJaxRs2 implements ICteRestArticulo{
	private static final int PAUSA_MAX = 5;
	
	@Autowired
	private InfoServicios infoServicios;
	private static Logger bitacora = LoggerFactory.getLogger(CteRestArticuloJaxRs2.class);
	
	@HystrixCommand(fallbackMethod = "fallbackGetArticuloJsonXml",
			        commandProperties = {
					//Timeout del llamado de 4 segundos
					@HystrixProperty(name= "execution.isolation.thread.timeoutInMilliseconds",value="4000"),
					
					//Ventana de evalución del circuit braker de 40 segundos
					@HystrixProperty(name="metrics.rollingStats.timeInMilliseconds",value="40000"),
					
					//Romper el circuito si se presentan 2 o más fallas en los 40 segundos
					@HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value="2"),
					
					//Esperar 60 segundos antes de volver evaluar el circuito
					@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value="60000")
			}
	)
	public Articulo getRemoto_ArticuloJsonXml(String cveArt) {
		String uriArticulo = "";
		Client cteJaxRs = null;
		bitacora.info("getRemoto_ArticuloJsonXml(" + cveArt
				+ ") Thread: " + Thread.currentThread().getId());
		try {
			this.hacerPausaNsegundos(this.getAleatorio());
			
			uriArticulo = infoServicios.getUriRecursoArticulo();
			
			cteJaxRs = ClientBuilder.newClient();
			Articulo articulo = cteJaxRs.target(uriArticulo)	
									    .path(cveArt)
								        .request(MediaType.APPLICATION_JSON)
								        .get(Articulo.class);
			bitacora.info(uriArticulo);
			bitacora.info("getRemoto_ArticuloJsonXml(" + cveArt 
					+ "), " + "Valor recuperado:" + articulo);
			return articulo;
		}
		catch(Exception ex) {
			String msjError = "getRemoto_ArticuloJsonXml("
					+ cveArt + ")"
					+ ": GET " + uriArticulo + "?clave=" + cveArt + ", en Xml/JSon. / "
					+ ex.getClass().getName() + ":" + ex.getMessage();
			bitacora.error(msjError);
			throw new CteException(msjError,ex);
		}
		finally {
			if(cteJaxRs != null)
				cteJaxRs.close();
		}
	}
	public Articulo fallbackGetArticuloJsonXml(String cveArt) {
		bitacora.warn("fallbackGetArticuloJsonXml(" + cveArt
				+ ")");
		return new Articulo("Indisponible", "Servicio no disponible: Reintente más tarde", "reporte a soporte",new BigDecimal(0));
	}
	
	public String getRemoto_ArticuloText(String cveArt) {
		String uriArticulo = "";
		Client cteJaxRs = null;
		try {
			uriArticulo = infoServicios.getUriRecursoArticulo();
			cteJaxRs = ClientBuilder.newClient();
			String cadArticulo = cteJaxRs.target(uriArticulo)
					  				 .path(cveArt)
								     .request(MediaType.TEXT_PLAIN)
								     .get(String.class);
			
			bitacora.info(uriArticulo);
			bitacora.info("getRemoto_ArticuloText(" + cveArt +"): Valor recuperado:" + cadArticulo);
			return cadArticulo;
		}
		catch(Exception ex) {
			String msjError = "getRemoto_ArticuloText("
					+ cveArt + ")"
					+ ": GET " + uriArticulo + "?clave=" + cveArt + ", en Texto. / "
					+ ex.getClass().getName() + ":" + ex.getMessage();
			bitacora.error(msjError);
			throw new CteException(msjError,ex);
		}
		finally {
			if(cteJaxRs != null)
				cteJaxRs.close();
		}
	}
	@HystrixCommand(fallbackMethod = "fallbackGetArticulosJsonXml"
			,commandProperties = {
					@HystrixProperty(
					//Timeout del llamado de 4 segundos
					 name= "execution.isolation.thread.timeoutInMilliseconds",value="4000"),
					//Ventana de evalución del circuit braker de 40 segundos
					@HystrixProperty(name="metrics.rollingStats.timeInMilliseconds",value="40000"),
					//Romper el circuito si se presentan 2 o más fallas en los 40 segundos
					@HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value="2"),
					//Esperar 60 segundos antes de volver evaluar el circuito
					@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value="60000")
			}
			)
	public Map<String, String> getRemoto_ArticulosJsonXml() {
		String uriArticulos = "";
		Client cteJaxRs = null;
		bitacora.info("getRemoto_ArticulosJsonXml() Thread: " + Thread.currentThread().getId());
		try {
			int numAleatorio = this.getAleatorio();
			bitacora.info("getRemoto_ArticulosJsonXml() :numAleatorio = " + numAleatorio);
			uriArticulos = numAleatorio % 3 == 0 ? infoServicios.getUriRecursoArticulo()
					                             : infoServicios.getUriRecursoArticuloTodos();
			this.hacerPausaNsegundos(numAleatorio);
			cteJaxRs = ClientBuilder.newClient();
			String cadArticulosJson = cteJaxRs.target(uriArticulos)
			                                  .request(MediaType.APPLICATION_JSON)
			                                  .get(String.class);
			bitacora.info(uriArticulos);
			bitacora.info("getRemoto_ArticulosJsonXml(): " + "Valor recuperado:" + cadArticulosJson);
			Map<String,String> cvesArticulos = this.ArticulosJsonToMap(cadArticulosJson);
			return cvesArticulos;
		}
		catch(Exception ex) {
			String msjError = "getRemoto_ArticulosJsonXml()"
					+ ": GET " + uriArticulos + ", en Json/Xml. / "
					+ ex.getClass().getName() + ":" + ex.getMessage();
			bitacora.error(msjError);
			throw new CteException(msjError,ex);
		}
		finally {
			if(cteJaxRs != null)
				cteJaxRs.close();
		}
	}
	
	public Map<String, String> fallbackGetArticulosJsonXml() {
		bitacora.warn("fallbackGetArticulosJsonXml()");
		Map<String,String> cvesArticulos = new HashMap<>();
		cvesArticulos.put("Servicio abajo:Reintente", "99999");
		return cvesArticulos;
	}
	
	private void hacerPausaNsegundos(int nSegundos) {
		bitacora.info("hacerPausaNsegundos(" + nSegundos + ")" );
		try {
			Thread.sleep(nSegundos * 1000);
		} catch (InterruptedException e) {
			bitacora.error("hacerPausaNsegundos(" + nSegundos + "): " 
		                      + e.getClass().getName() + ":" + e.getMessage());
		}
		bitacora.info("hacerPausaNsegundos(" + nSegundos + "): Fin" );
	}
	
	private int getAleatorio() {
		int num = (int) (Math.random() * 10000);
		return num % (PAUSA_MAX + 1);
	}
	
	private Map<String, String> ArticulosJsonToMap(String cadArticulosJson) {
		Map<String,String> cvesArticulos = new TreeMap<>();
		JsonReader jsonReader = Json.createReader(new StringReader(cadArticulosJson));
		JsonArray jsonArticulos = jsonReader.readArray(); 
		for(int i=0; i<jsonArticulos.size(); i++) {
			JsonObject artIjson = jsonArticulos.getJsonObject(i);
			String claveI = artIjson.getString("clave");
			String nombreI = artIjson.getString("nombre");
			cvesArticulos.put(nombreI.toLowerCase() + ":" + claveI , claveI);
		}
		return cvesArticulos;
	}
	
	public String postRemoto_ArticuloJson(Articulo artI) {
		String uriArticulo = infoServicios.getUriRecursoArticulo();
		Client cteJaxRs = null;
		try {
			Entity<Articulo> bodyPeticion = Entity.entity(artI, MediaType.APPLICATION_JSON);
			cteJaxRs = ClientBuilder.newClient();
			String resultadoInsert = cteJaxRs.target(uriArticulo)
					                         .request(MediaType.TEXT_PLAIN)
					                         .post(bodyPeticion, String.class);
			bitacora.info(uriArticulo);
			bitacora.info("postRemoto_ArticuloJson(" + artI
					+ ") " + "Valor recuperado:" + resultadoInsert);
			return resultadoInsert;
		}
		catch(Exception ex) {
			String msjError = "postRemoto_ArticuloJson("
					+ artI + ")"
					+ ": POST " + uriArticulo + ", Body en Json"
					+ ex.getClass().getName() + ":" + ex.getMessage();
			bitacora.error(msjError);
			throw new CteException(msjError,ex);
		}
		finally {
			if(cteJaxRs != null)
				cteJaxRs.close();
		}
	}

	public String postRemoto_ArticuloFormUrlEncoded(HttpServletRequest request) {
		String uriArticulo = infoServicios.getUriRecursoArticulo();
		Client cteJaxRs = null;
		try {
			Entity<Form> bodyAltaArticulo = getBodyAltaArticulo(request);
			cteJaxRs = ClientBuilder.newClient();
			String resultadoInsert = cteJaxRs.target(uriArticulo)
						                     .request(MediaType.TEXT_PLAIN)
						                     .post(bodyAltaArticulo, String.class);
			bitacora.info(uriArticulo);
			bitacora.info("postRemoto_ArticuloFormUrlEncoded() " + "Valor recuperado:" + resultadoInsert);
			return resultadoInsert;
		}
		catch(Exception ex) {
			String msjError = "postRemoto_ArticuloFormUrlEncoded("
					+ request.getRequestURI() + "): "
					+ "POST " + uriArticulo + ", Body en FormUrlEncoded, "
					+ ex.getClass().getName() + ":" + ex.getMessage();
			bitacora.error(msjError);
			throw new CteException(msjError,ex);
		}
		finally {
			if(cteJaxRs != null)
				cteJaxRs.close();
		}
		
	}
	private Entity<Form> getBodyAltaArticulo(HttpServletRequest request) {
		Form unaForma = new Form();
		unaForma.param("clave", request.getParameter("clave"));
		unaForma.param("nombre", request.getParameter("nombre"));
		unaForma.param("descripcion", request.getParameter("descripcion"));
		unaForma.param("costo", request.getParameter("costo"));
		unaForma.param("precio", request.getParameter("precio"));
		unaForma.param("existencia", request.getParameter("existencia"));

	    Entity<Form> bodyPeticion = 
	    		 Entity.entity(unaForma, MediaType.APPLICATION_FORM_URLENCODED);
		return bodyPeticion;
	}
	public int getRemotoAsincrono_Articulos(List<String> respuestas) {
		int folio = 0;
		final Object LOCK = new Object();
		CallBackArticulos callBack = null;
		synchronized (LOCK) {
			 respuestas.add("");
			 folio = respuestas.size() - 1;
			 callBack = new  CallBackArticulos(respuestas, folio);
		}
		String uriArticulos = null;
		Client cteJaxRs = null;
		try {
			uriArticulos = infoServicios.getUriRecursoArticulo() + "/todos_lento";
			cteJaxRs = ClientBuilder.newClient();
	
			cteJaxRs.target(uriArticulos)
                    .request(MediaType.APPLICATION_XML)
                    .async()
                    .get(callBack);
			bitacora.info(uriArticulos);
			bitacora.info("getRemotoAsincrono_Articulos( " + respuestas + ")");
			return folio;
		}
		catch(Exception ex) {
			String msjError = "getRemotoAsincrono_Articulos("
					+ respuestas + "): "
					+ "GET " + uriArticulos + ", en Xml, "
					+ ex.getClass().getName() + ":" + ex.getMessage();
			bitacora.error(msjError);
			throw new CteException(msjError,ex);
			
		}
	}
}
