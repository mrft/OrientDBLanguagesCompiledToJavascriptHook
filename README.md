This is a hook for OrientDB that will add some functionality to functions.
OrientDB functions are stored in the database as docuemtns of class OFunction.
They have a 'language' property that can be sql or Javascript, 
and a 'code' property that contains the sql or javascript code.
What this hook will do is, it will check if the saved OFunction contains 
a property called 'icedcoffeescript' or 'livescript'. If it does, 
the 'code' will be replaced by the compiled-to-javascript version of the script.

This way, you can also write server-side functions in either coffeescript or livescript.
It should be quite easy to add otehr languages, if there is a javascript compiler available.

Installing the hook is quite easy:

- Use ant to build the IcedCoffeeScriptHook.jar file in the '/target' folder (bad name since it also supports LiveScript but anyway)
- copy the IcedCoffeeScriptHook.jar file to your /orientdb-community-1.7.x/lib directory
- add a section called hooks to /orientdb-community-1.7.x/conf/orientdb-server-config.xml (example below)
	<orient-server>
		...
		<hooks>
			<hook class="be.vsko.orientdb.IcedCoffeeScriptHook" position="REGULAR"/>
		</hooks>
	</orient-server>
- restart OrientDB
