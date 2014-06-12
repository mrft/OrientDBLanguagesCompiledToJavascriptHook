/**
 *
 */
package be.vsko.orientdb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;

import javax.script.*;

/**
 * This hook should enable us to store a OFunction object with an extra field
 * called icedcoffeescript or something, and whenever the record is updated, we
 * should regenerate the javascript by compiling the icedcoffeescript code.
 *
 * @author ftilkin
 */
public class IcedCoffeeScriptHook extends ODocumentHookAbstract implements ORecordHook {

	private static ScriptEngineManager scriptEngineManager = null;
	private static ScriptEngine scriptEngine = null;
	private static String icedcoffeescriptPropertyName = "icedcoffeescript";
	private static String livescriptPropertyName = "livescript";

	////Not used: Property 'language' will still be Javascript !
	//private static String icedcoffeescriptLanguageName = "IcedCoffeeScript";
	//private static String livescriptLanguageName = "LiveScript";

	/**
	 * Needed to protect the script-engine against multi-threaded access
	 */
	private final static ReentrantLock lock = new ReentrantLock( true );


	/**
	 * @throws ScriptException
	 */
	public IcedCoffeeScriptHook() throws ScriptException {
		//System.out.println( "\n[IcedCoffeeScriptHook] CONSTRUCTOR" );
	    setIncludeClasses( "OFunction" );


//        String myJavaString = "hello this is a string defined in JAVA that I will try to use in javascript";
//        scriptEngine.put( "myJavaString", myJavaString );
//        scriptEngine.eval( "var myJSString = 'Javascript string concatenated with the following java string: ' + myJavaString ;" );
//        scriptEngine.eval( "java.lang.System.out.println( \"javascript string result = \" + myJSString + \" \" + typeof myJavaString );" );

        /*
        scriptEngine.eval( "var x = 1 + 1;" );
        scriptEngine.eval( "java.lang.System.out.println( \"javascript result = \" + x + \" \" + typeof CoffeeScript );" );
        //scriptEngine.eval( "console.log( \"result = \" + x );" );
        //scriptEngine.eval( "for ( var f in CoffeeScript ) { java.lang.System.out.println( f ); }" );
        scriptEngine.eval( "CoffeeScript.eval( 'if x == 2 then y = x else y = 555\\njava.lang.System.out.println( \"coffee: y = #{y}\" );' );" );

        scriptEngine.eval(
        		"var ics = 'if x == 2 then z = x else z = 555';" +
        		"var icsCompiled = \"\";" +
        		"try { " +
        		"	icsCompiled = CoffeeScript.compile( ics );" +
        		"}" +
        		"catch (e) {" +
        		"	icsCompiled = 'return \"' + e + '\";';" +
        		"}" +
        		""
        	);

        scriptEngine.eval( "java.lang.System.out.println( icsCompiled );" );

        String icsCompiled = (String) scriptEngine.eval( "icsCompiled;" );
        System.out.println( "The string in JAVA: " + icsCompiled );
        */

        /*
        // JavaScript code in a String
        String script = "function hello(name) { print('Hello, ' + name); }";
        // evaluate script
        scriptEngine.eval(script);

        // javax.script.Invocable is an optional interface.
        // Check whether your script engine implements or not!
        // Note that the JavaScript engine implements Invocable interface.
        Invocable inv = (Invocable) scriptEngine;

        // invoke the global function named "hello"
        inv.invokeFunction("hello", "Scripting!!" );
        */
}

	/* (non-Javadoc)
	 * @see com.orientechnologies.orient.core.hook.ORecordHook#getDistributedExecutionMode()
	 */
	@Override
	public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
		return DISTRIBUTED_EXECUTION_MODE.BOTH;
	}

	public RESULT onRecordBeforeCreate( ODocument iDocument ) {
		System.out.println( "[IcedCoffeeScriptHook] onRecordBeforeCreate" );
		return updateJavaScriptCodeIfNecessary( iDocument );
	}


//	public RESULT onRecordBeforeRead( ODocument iDocument ) {
//		//System.out.println( "[IcedCoffeeScriptHook] onRecordBeforeRead" );
//		if ( iDocument.field( icedcoffeescriptPropertyName ) != null ) {
//			//System.out.println( "[IcedCoffeeScriptHook] contains icecoffeescript property, so change function language to IcedCoffeeScript" );
//			iDocument.field( "language", icedcoffeescriptLanguageName );
//			return RESULT.RECORD_CHANGED;
//		}
//		else {
//			//System.out.println( "[IcedCoffeeScriptHook] doesn't contain icecoffeescript property, so DON'T change anything..." );
//			return RESULT.RECORD_NOT_CHANGED;
//		}
//	}
//	public void onRecordAfterRead(ORecord<?> iRecord){}
	public RESULT onRecordBeforeUpdate( ODocument iDocument ) {
		System.out.println( "[IcedCoffeeScriptHook] onRecordBeforeUpdate" );
		return updateJavaScriptCodeIfNecessary( iDocument );
	}


	private RESULT updateJavaScriptCodeIfNecessary(ODocument iDocument) {
		//If record contains a 'icedcoffeescript' field, try to compile it and update the javascript field
		//System.out.println( icedcoffeescriptPropertyName + " is " + (null == iDocument.field( icedcoffeescriptPropertyName ) ? "null, so don't try to compile" : "not null so let's try to compile it and update the code field"));
		//iDocument.reload();

		boolean hasIcedCoffeeScriptProperty = ( null != iDocument.field( icedcoffeescriptPropertyName ) );
		boolean hasLiveScriptProperty = ( null != iDocument.field( livescriptPropertyName ) );

		String commentAddedToCompiledCode = "";
		if ( hasIcedCoffeeScriptProperty && hasLiveScriptProperty ) {
			iDocument.field( livescriptPropertyName, (String)null );
			hasLiveScriptProperty = false;
			commentAddedToCompiledCode = "/* OFunction contained both an " + icedcoffeescriptPropertyName + " and a " + livescriptPropertyName + ". " + livescriptPropertyName + " will be removed... */";
		}

		if ( hasIcedCoffeeScriptProperty || hasLiveScriptProperty ) {

//			if ( ( (String) iDocument.field( "language" ) ).compareTo( icedcoffeescriptLanguageName ) != 0 ) {
//				//if language is something else, remove that property
//				iDocument.field( icedcoffeescriptPropertyName, (String)null );
//				return ORecordHook.RESULT.RECORD_CHANGED;
//			}
//			else {
//				//if language is IcedCoffeeScript, compile !

			    if ( hasIcedCoffeeScriptProperty ) {
			    	iDocument.field( "code", commentAddedToCompiledCode + "\n" + getCompiledIcedCoffeeScriptFunctionBody( (String) iDocument.field( icedcoffeescriptPropertyName ), (List<String>) iDocument.field( "parameters" ) ) );
			    }
			    else if ( hasLiveScriptProperty ) {
			    	iDocument.field( "code", commentAddedToCompiledCode + "\n" + getCompiledLiveScriptFunctionBody( (String) iDocument.field( livescriptPropertyName ), (List<String>) iDocument.field( "parameters" ) ) );
			    }
		        //System.out.println( "Successfully updated the code field!" );
				return ORecordHook.RESULT.RECORD_CHANGED;
//			}
		}
		else {
			return ORecordHook.RESULT.RECORD_NOT_CHANGED;
		}
	}

	/**
	 * Tries to load a .js file found on the classpath, and execute it in the scriptEngine
	 *
	 * scriptEngine is supposed to be initialized already !!!
	 *
	 * @param filename
	 */
	static public void requireJavaScriptResource( String filename ) {
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

//	public void onRecordAfterUpdate(ORecord<?> iRecord){
		//If record contains a 'iced' field, try to compile it and update the javascript field
//	}
//	public void onRecordBeforeDelete(ORecord<?> iRecord){}
//	public void onRecordAfterDelete(ORecord<?> iRecord){}

	private static void initScriptEngineIfNecessary() {
	    //Only do this once !!!
	    if ( scriptEngineManager == null || scriptEngine == null ) {
			System.out.println( "IcedCoffeeScriptHook() CONSTRUCTOR: I guess this is the first time, so please have some patience while we initalize the javascript environment and the necessary compilers." );

	        scriptEngineManager = new ScriptEngineManager();
	        scriptEngine = scriptEngineManager.getEngineByName( "JavaScript" );

	        requireJavaScriptResource( "icedcoffeescript.js" );
	        requireJavaScriptResource( "livescript.js" );
	    }
	}

	public static String getCompiledIcedCoffeeScriptFunctionBody( String script, List<String> parameters ) {
		lock.lock();
		try {

			initScriptEngineIfNecessary();

			if ( parameters == null ) parameters = new ArrayList<String>();
	        String compiledScript = null;
	        String compilerError = null;

	        // create a string of the form ( par1, par2 ) that we will use to add a function definition around the bare icedcoffeescript code
	        String parametersScriptPart = "(";
	        for ( String p : parameters ) {
	        	parametersScriptPart += ( parametersScriptPart.length() == 1 ? "" : ", " ) + p;
	        }
	        parametersScriptPart += ")";

	        //We need to define a coffeescript function with the given parameters
	        String preparedScript = "functionbody = " + parametersScriptPart + " ->\n";

	        //to make the code a part of this function body it has to be indented (we'll read line by line and add a tab character)
	        BufferedReader reader = new BufferedReader( new StringReader( script ) );
	        try {
		        String line = reader.readLine();
		        while ( line != null ) {
		        	preparedScript += "\t" + line + "\n";
		        	line = reader.readLine();
		        }
	        }
	        catch (Exception e) {
	        	compilerError = "[ScriptException while trying to compile] " + e.getMessage();
	        }


	        try {
				//scriptEngine.eval( "var x = 1 + 1;" );
		        //scriptEngine.eval( "java.lang.System.out.println( \"javascript result = \" + x + \" \" + typeof CoffeeScript );" );
		        //scriptEngine.eval( "console.log( \"result = \" + x );" );
		        //scriptEngine.eval( "for ( var f in CoffeeScript ) { java.lang.System.out.println( f ); }" );
		        //scriptEngine.eval( "CoffeeScript.eval( 'if x == 2 then y = x else y = 555\\njava.lang.System.out.println( \"coffee: y = #{y}\" );' );" );

	        	scriptEngine.put( "ics", preparedScript );
		        scriptEngine.eval(
		        		//"var ics = script;" +
		        		"var icsCompiled = \"\";" +
		        		"try { " +
		        		"	icsCompiled = CoffeeScript.compile( ics );" +
		        		"}" +
		        		"catch (e) {" +
		        		"	icsCompiled = 'return \"[Compiler Error] ' + e + '\";';" +
		        		"}" +
		        		""
		        	);

		        //scriptEngine.eval( "java.lang.System.out.println( icsCompiled );" );

		        compiledScript = (String) scriptEngine.eval( "icsCompiled;" );
		        System.out.println( "The compiled icedcoffeescript function = " + compiledScript );

		        if ( compiledScript.startsWith( "return" ) ) {
		        	//Compile error, do nothing
		        }
		        else {
			        // IcedCoffeeScript compiler creates code like below
			        // --> function() { var functionbody; functionbody = function() { return 1 + 1; }; }).call(this);
			        // , but since this should be a function body, we'll strip OFF the
			        // --> function() { ... }).call(this);
			        // parts
			        System.out.println( "We'll strip some things off the head and the tail " );
			        compiledScript = compiledScript.substring( 13, compiledScript.length() - 15 );
			        System.out.println( compiledScript );
			        System.out.println( "And we'll add a call to functionbody with the right prameters..." );
			        compiledScript += "\n\n" + "return functionbody" + parametersScriptPart + ";";
			        System.out.println( compiledScript );
		        }
			} catch (ScriptException e) {
				compilerError = "[Exception while trying to compile] " + e.getMessage();
			}

	        if ( compilerError != null ) {
	        	//scriptEngine.put( "compilerError", compilerError );
	        	//icsCompiled = scriptEngine.eval( "" )
				compiledScript = "return \"" + compilerError + "\";";
	        }

	        return compiledScript;
	        /*
	        // JavaScript code in a String
	        String script = "function hello(name) { print('Hello, ' + name); }";
	        // evaluate script
	        scriptEngine.eval(script);

	        // javax.script.Invocable is an optional interface.
	        // Check whether your script engine implements or not!
	        // Note that the JavaScript engine implements Invocable interface.
	        Invocable inv = (Invocable) scriptEngine;

	        // invoke the global function named "hello"
	        inv.invokeFunction("hello", "Scripting!!" );
	        */
		}
		finally {
			lock.unlock();
		}
	}


	public static String getCompiledLiveScriptFunctionBody( String script, List<String> parameters ) {
		lock.lock();
		try {
			initScriptEngineIfNecessary();

			if ( parameters == null ) parameters = new ArrayList<String>();
	        String compiledScript = null;
	        String compilerError = null;

	        // create a string of the form ( par1, par2 ) that we will use to add a function definition around the bare icedcoffeescript code
	        String parametersScriptPart = "";
	        for ( String p : parameters ) {
	        	parametersScriptPart += ( parametersScriptPart.length() == 0 ? "(" : ", " ) + p;
	        }
	        if ( parametersScriptPart.length() > 0 )
	        	parametersScriptPart += ")";

	        //We need to define a coffeescript function with the given parameters
	        String preparedScript = "functionbody = " + parametersScriptPart + " -->\n";

	        //to make the code a part of this function body it has to be indented (we'll read line by line and add a tab character)
	        BufferedReader reader = new BufferedReader( new StringReader( script ) );
	        try {
		        String line = reader.readLine();
		        while ( line != null ) {
		        	preparedScript += "\t" + line + "\n";
		        	line = reader.readLine();
		        }
	        }
	        catch (Exception e) {
	        	compilerError = "[ScriptException while trying to compile] " + e.getMessage();
	        }


	        try {
	        	System.out.println( "==== Trying to compile ==== \n\n" + preparedScript );
	        	scriptEngine.put( "preparedScript", preparedScript );
		        scriptEngine.eval(
		        		//"var ics = script;" +
		        		"var compiledScript = \"\";" +
		        		"try { " +
		        		"	compiledScript = LiveScript.compile( preparedScript );" +
		        		"}" +
		        		"catch (e) {" +
		        		"	compiledScript = 'return \"[Compiler Error] ' + e + '\";';" +
		        		"}" +
		        		""
		        	);

		        //scriptEngine.eval( "java.lang.System.out.println( icsCompiled );" );

		        compiledScript = (String) scriptEngine.eval( "compiledScript;" );
		        System.out.println( "The compiled LiveScript function = " + compiledScript );

		        if ( compiledScript.startsWith( "return" ) ) {
		        	//Compile error, do nothing
		        }
		        else {
			        // LiveScript compiler creates code like below
			        // --> function() { var functionbody; functionbody = function() { return 1 + 1; }; }).call(this);
			        // , but since this should be a function body, we'll strip OFF the
			        // --> function() { ... }).call(this);
			        // parts
			        System.out.println( "We'll strip some things off the head and the tail " );
			        compiledScript = compiledScript.substring( 13, compiledScript.length() - 15 );
			        System.out.println( compiledScript );
			        System.out.println( "And we'll add a call to functionbody with the right prameters..." );
			        compiledScript += "\n\n" + "return functionbody" + ( parametersScriptPart.length() > 0 ? parametersScriptPart : "()" ) + ";";
			        System.out.println( compiledScript );
		        }
			} catch (ScriptException e) {
				compilerError = "[Exception while trying to compile] " + e.getMessage();
			}

	        if ( compilerError != null ) {
	        	//scriptEngine.put( "compilerError", compilerError );
	        	//icsCompiled = scriptEngine.eval( "" )
				compiledScript = "return \"" + compilerError + "\";";
	        }

	        return compiledScript;
		}
		finally {
			lock.unlock();
		}
	}

}
