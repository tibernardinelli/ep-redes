package epredes;

import java.util.HashMap;
import java.util.Map;

import javax.swing.SpringLayout.Constraints;

import epredes.programas.Programaweb;

public class Executor {

	private static final String PKG = "epredes.programas.%s";

	static RetornoExecucao execute(String retorno, String parameters) {
		try {
			Class<?> classe = Class.forName(String.format(PKG, retorno));
			Programaweb pw = (Programaweb) classe.newInstance();

			return pw.Executa(extraiParametros(parameters));
		} catch (Exception e) {
			e.printStackTrace();
			return new RetornoExecucao("500", "", e.getMessage());
		}
	}

	private static Map<String, String> extraiParametros(String params) {
		Map<String, String> retorno = new HashMap<>();
		String[] _params;
		if (params.contains("&")) {
			_params = params.split("&");
		} else {
			_params = new String[] { params };
		}
		for (String s : _params) {
			if (s.contains("=")) {
				String[] split = s.split("=");
				retorno.put(split[0], split[1]);
			}
		}
		return retorno;
	}

}
