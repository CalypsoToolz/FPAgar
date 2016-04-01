package ru.calypso.ogar.server.util;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.log4j.Logger;

import ru.calypso.ogar.server.util.compiler.Compiler;
import ru.calypso.ogar.server.util.compiler.MemoryClassLoader;
import ru.calypso.ogar.server.util.listeners.Listener;
import ru.calypso.ogar.server.util.listeners.ListenerList;
import ru.calypso.ogar.server.util.listeners.OnInitScriptListener;
import ru.calypso.ogar.server.util.listeners.ScriptListener;

public class ScriptsLoader
{
	public class ScriptListenerImpl extends ListenerList<ScriptsLoader>
	{
		public void init()
		{
			for(Listener<ScriptsLoader> listener : getListeners())
				if(OnInitScriptListener.class.isInstance(listener))
					((OnInitScriptListener) listener).onInit();
		}
	}

	private static final Logger _log = Logger.getLogger(ScriptsLoader.class);

	private static final ScriptsLoader _instance = new ScriptsLoader();

	public static ScriptsLoader getInstance()
	{
		return _instance;
	}

	private final Map<String, Class<?>> _classes = new TreeMap<String, Class<?>>();
	private final ScriptListenerImpl _listeners = new ScriptListenerImpl();

	private ScriptsLoader()
	{
		load();
	}

	/**
	 * Вызывается при загрузке сервера. Загружает все скрипты из scripts. Не инициирует объекты и обработчики.
	 */
	private void load()
	{
		_log.info("Scripts: Loading...");

		List<Class<?>> classes = load(new File("scripts"));
		if(classes.isEmpty())
		{
			throw new Error("Failed loading scripts!");
		}

		_log.info("Scripts: Loaded " + classes.size() + " classes.");

		Class<?> clazz;
		for(int i = 0; i < classes.size(); i++)
		{
			clazz = classes.get(i);
			_classes.put(clazz.getName(), clazz);
		}
	}

	/**
	 * Вызывается при загрузке сервера. Инициализирует объекты и обработчики.
	 */
	public void init()
	{
		for(Class<?> clazz : _classes.values())
			init(clazz);

		_listeners.init();
	}

	/**
	 * Загрузить все классы из scripts/@target
	 *
	 * @param target путь до класса, или каталога со скриптами
	 * @return список загруженых скриптов
	 */
	public List<Class<?>> load(File target)
	{
		Collection<File> scriptFiles = Collections.emptyList();

		if(target.isFile())
		{
			scriptFiles = new ArrayList<File>(1);
			scriptFiles.add(target);
		}
		else if(target.isDirectory())
		{
			scriptFiles = FileUtils.listFiles(target, FileFilterUtils.suffixFileFilter(".java"), FileFilterUtils.directoryFileFilter());
		}

		if(scriptFiles.isEmpty())
			return Collections.emptyList();

		List<Class<?>> classes = new ArrayList<Class<?>>();
		Compiler compiler = new Compiler();

		if(compiler.compile(scriptFiles))
		{
			MemoryClassLoader classLoader = compiler.getClassLoader();
			for(String name : classLoader.getLoadedClasses())
			{
				//Вложенные класс
				if(name.contains(ClassUtils.INNER_CLASS_SEPARATOR))
					continue;

				try
				{
					Class<?> clazz = classLoader.loadClass(name);
					if(Modifier.isAbstract(clazz.getModifiers()))
						continue;
					classes.add(clazz);
				}
				catch(ClassNotFoundException e)
				{
					_log.error("Scripts: Can't load script class: " + name, e);
					classes.clear();
					break;
				}
			}
		}

		return classes;
	}


	private Object init(Class<?> clazz)
	{
		Object o = null;

		try
		{
			if(ClassUtils.isAssignable(clazz, ScriptListener.class))
			{
				o = clazz.newInstance();

				_listeners.add((ScriptListener)o);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		return o;
	}


	public Map<String, Class<?>> getClasses()
	{
		return _classes;
	}
}