package be.vsko.orientdb;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Test {

	/**
	 * @param args
	 * @throws ScriptException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException, ScriptException {
		//IcedCoffeeScriptHook icsHook = new IcedCoffeeScriptHook();
		System.out.println( "IcedCoffeescript compiled = " + IcedCoffeeScriptHook.getCompiledLiveScriptFunctionBody( "1+1", null ) );

		System.out.println( "Livescript compiled = " + IcedCoffeeScriptHook.getCompiledLiveScriptFunctionBody( "1+1", null ) );


//	    ScriptEngineManager scriptEngineManager = null;
//		ScriptEngine scriptEngine  = null;
//		if ( scriptEngineManager == null || scriptEngine == null ) {
//			System.out.println( "IcedCoffeeScriptHook() CONSTRUCTOR: I guess this is the first time, so please have some patience while we initalize the javascript environment and the necessary compilers." );
//
//	        scriptEngineManager = new ScriptEngineManager();
//	        scriptEngine = scriptEngineManager.getEngineByName( "JavaScript" );
//
//
//	        String request = "Hello";
//
//	        scriptEngine.eval( "this.x = \"Hello\";" );
//	        scriptEngine.put( "r", request );
//
//	        scriptEngine.eval( "r = r + \" 2nd Hello\";" );
//
//	        String result = (String) scriptEngine.eval( "r" );
//
//	        System.out.println( "Result = " + result );
//
//	        //scriptEngine.eval( "java.lang.System.out.println( \"javascript result = \" + this.x + \" \" + typeof this.x );" );
//
//	        //requireJavaScriptResource( "livescript.js", scriptEngine );
//	    }

	}



	/**
	 * Tries to load a .js file found on the classpath, and execute it in the scriptEngine
	 *
	 * scriptEngine is supposed to be initialized already !!!
	 *
	 * @param filename
	 * @return
	 */
	private static void requireJavaScriptResource( String filename, ScriptEngine scriptEngine ) {
		InputStream is = IcedCoffeeScriptHook.class.getResourceAsStream( filename );
		//FileReader r = new FileReader( "icedcoffeescript.js" );
		InputStreamReader r = new InputStreamReader( is );
		try {
			scriptEngine.eval( r );
		}
		catch ( ScriptException e ) {
			throw new RuntimeException( "Failed to load " + filename + " because of a ScriptException ( '" + e.getMessage() + "' ) This mustn't happen..." );
		}
	}

}
