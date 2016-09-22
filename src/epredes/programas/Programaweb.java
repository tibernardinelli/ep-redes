package epredes.programas;

import java.io.IOException;
import java.util.Map;

import epredes.Executor;
import epredes.RetornoExecucao;

public interface Programaweb {

	RetornoExecucao Executa(Map<String, String> params) throws IOException;
	
}
