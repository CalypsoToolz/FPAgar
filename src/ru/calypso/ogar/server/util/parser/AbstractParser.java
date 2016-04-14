package ru.calypso.ogar.server.util.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ru.calypso.ogar.server.util.LoggerObject;
import ru.calypso.ogar.server.util.holder.AbstractHolder;

/**
 * Author: VISTALL Date: 18:35/30.11.2010
 */

public abstract class AbstractParser<H extends AbstractHolder> extends LoggerObject {
	protected final H _holder;

	protected String _currentFile;
	protected SAXReader _reader;

	protected AbstractParser(H holder) {
		_holder = holder;
		_reader = new SAXReader();
		_reader.setValidation(true);
		_reader.setErrorHandler(new ErrorHandlerImpl(this));
	}

	protected void initDTD(File f) {
		_reader.setEntityResolver(new SimpleDTDEntityResolver(f));
	}

	protected void parseDocument(InputStream f, String name) throws Exception {
		_currentFile = name;

		org.dom4j.Document document = _reader.read(f);

		readData(document.getRootElement());
	}

	protected abstract void readData(Element rootElement) throws Exception;

	protected abstract void parse();

	protected H getHolder() {
		return _holder;
	}

	public String getCurrentFileName() {
		return _currentFile;
	}

	public void load() {
		parse();
		_holder.process();
		_holder.log();
	}

	public void reload() {
		info("reload start...");
		_holder.clear();
		load();
	}

	public class ErrorHandlerImpl implements ErrorHandler {
		private AbstractParser<?> _parser;

		public ErrorHandlerImpl(AbstractParser<?> parser) {
			_parser = parser;
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			_parser.warn("File: " + _parser.getCurrentFileName() + ":" + exception.getLineNumber() + " warning: "
					+ exception.getMessage());
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			_parser.error("File: " + _parser.getCurrentFileName() + ":" + exception.getLineNumber() + " error: "
					+ exception.getMessage());
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			_parser.error("File: " + _parser.getCurrentFileName() + ":" + exception.getLineNumber() + " fatal: "
					+ exception.getMessage());
		}
	}

	public class SimpleDTDEntityResolver implements EntityResolver {
		private String _fileName;

		public SimpleDTDEntityResolver(File f) {
			_fileName = f.getAbsolutePath();
		}

		@Override
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			return new InputSource(_fileName);
		}
	}
}
