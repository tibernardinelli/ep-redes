package epredes.programas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import epredes.RetornoExecucao;

public class LeitorLog implements Programaweb {

	@Override
	public RetornoExecucao Executa(Map<String, String> params) throws IOException {
		int linhade = Integer.parseInt(params.get("linhade"));
		int linhaate = Integer.parseInt(params.get("linhaate"));
		System.out.println(linhade);
		System.out.println(linhaate);
		Path path = Paths.get("requests.log");
		StringBuffer sb = new StringBuffer();
		List<String> teste = Files.readAllLines(path);
		System.out.println(teste.size());
		for (int i = 0; i < teste.size(); i++) {
			if (i >= linhade && i <= linhaate) {
				sb.append(teste.get(i) + "<br>");
			}
		}
		return new RetornoExecucao("200", "text/plain;charset=utf-8", sb.toString());
	}

}
