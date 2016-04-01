package NickProtection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.calypso.ogar.server.events.IPlayerEventHandler;
import ru.calypso.ogar.server.events.PlayerEventHandler;
import ru.calypso.ogar.server.events.player.PlayerNameChangeEvent;
import ru.calypso.ogar.server.util.listeners.OnInitScriptListener;

/**
 * 
 * @author Calypso - Freya Project team
 * <p>Для работы необходимо отсылать с клиента в пакете ника (ID 0) данные в виде "ник:::::пароль"
 * <p>Ники/пароли загружаются с сайта (удобно если запущено несколько серверов
 *																- не нужно синхронизировать список)
 * <p>Ники/пароли добавлять по 1 на строку, если это клан тег, то указывать [так]!
 * <p>Если необходима авторизация на веб-сервере, то указать данные здесь: {@link userAndPass}
 **/
public class NickProtection extends IPlayerEventHandler implements OnInitScriptListener
{
	private static final Logger _log = Logger.getLogger(NickProtection.class);
	private final Map<String, String> authData = new HashMap<String, String>();
	// откуда берем логины:пароли
	private final String urlForParse = "http://ogar.pp.ua/data/passwords.list";
	// требует ли веб сервер авторизацию?
	private final boolean webServerUseAuth = true;
	// данные для авторизации (пользователь:пароль)
	private final String userAndPass = "calypso:calypsopass";

	@Override
	public void onInit() {
		PlayerEventHandler.getInstance().registerScript(this);
		loadAuthData();
	}
    
	public void loadAuthData()
	{
		handlePageCode(getPage(urlForParse));
	}

	@Override
	public void onNameChange(PlayerNameChangeEvent event)
	{
		// преобразуем данные в массив, где 0 элемент - ник, 1 - пароль
		String[] data = event.getName().split(":::::");
		String name, pass = "";
        // указан и ник и пароль
        if(data.length > 1)
        {
        	name = data[0];
        	pass = data[1];
        }
        // указан только ник
        else if(data.length == 1)
        	name = data[0];
        // вообще ничего не указано
        else
        	name = String.valueOf(event.getPlayer().hashCode());
        
        // если в нике используется клан тег
        if(isClan(name))
        {
        	// составляем клан тег
        	String clanTag = "[" + name.substring(1, name.indexOf(']')) + "]";
        	// если тег под паролем и пароль не верный
        	if(isProtected(clanTag) && !checkPass(clanTag, pass))
        		name = "Wrong Clan Pass";
        }
        // если это не клан тег и ник запаролен и указан не верный пароль
        else if(isProtected(name) && !checkPass(name, pass))
    		name = "Wrong Password";
        
        // установим получившийся ник игроку
        event.setName(name);
    	_log.info("Player has changed name to " + event.getName() + "!");
	}

	/**
	 * Проверка пароля на валидность
	 * @param nick - запароленный ник (должен быть добавлен в {@code authData}
	 * @param pass - проверяемый пароль
	 * @return true, если пароль валидный
	 */
	public boolean checkPass(String nick, String pass)
	{
		return authData.get(nick.toLowerCase()).equals(pass);
	}

	/**
	 * Проверить защищен ли ник паролем (есть ли он в {@code authData})
	 * @param nick - проверяемый ник
	 * @return true, если он защищен
	 */
	public boolean isProtected(String nick)
	{
		return authData.containsKey(nick.toLowerCase());
	}

	/**
	 * Проверить содержит ли ник клан-тег ([bla] nick = true, b[la] nick = false)
	 * @param nick - проверяемый тег
	 * @return true, если ник содержит клан-тег
	 */
	public boolean isClan(String nick)
	{
		return nick.indexOf("[") == 0 && nick.indexOf("]") != -1;
	}

	public String getPage(String urlsite)
    {
        String outx= "";
        try
        {
            URL url = new URL(urlsite);
            
            URLConnection conn = url.openConnection();
            if(webServerUseAuth)
	            conn.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode(userAndPass.getBytes())));
            InputStream is = conn.getInputStream();
            outx = getPageCode(is);
            is.close();
        }
        catch (Exception e)
        {
            _log.error("Error", e);
        }
        return outx;  
    };

	public String getPageCode(InputStream is) {
		byte[] code = new byte[32756];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while (true) {
				int n = is.read(code);
				if (n == -1) {
					break;
				}
				if (n > 0) {
					baos.write(code, 0, n);
				}
			}
			baos.close();
		} catch (IOException e) {
			_log.error("Error while getPageCode()", e);
		}
		String s = baos.toString();
		return s;
	}

	public void handlePageCode(String code)
	{
		String [] lines = code.split("\n");
		for(String line : lines)
		{
			if(!line.isEmpty())
			{
				try{
					authData.put(line.split(":")[0].toLowerCase(), line.split(":")[1]);
				}catch (ArrayIndexOutOfBoundsException e) {
					_log.error("Error while handlePageCode()", e);
				}
			}
		}
		
		_log.info("[NickProtection] Auth-data size: " + authData.size());
	}
}
