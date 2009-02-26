package ch.qos.logback.classic.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import ch.qos.logback.classic.Level;

public class LoggingEventSDO implements ILoggingEvent, Serializable {

  private static final long serialVersionUID = 6553722650255690312L;

  private static final int NULL_ARGUMENT_ARRAY = -1;
  private static final String NULL_ARGUMENT_ARRAY_ELEMENT = "NULL_ARGUMENT_ARRAY_ELEMENT";

  private String threadName;
  private transient Level level;
  private String message;

  // we gain significant space at serialization time by marking
  // formattedMessage as transient and constructing it lazily in
  // getFormmatedMessage()
  private transient String formattedMessage;

  private transient Object[] argumentArray;

  private ThrowableProxy throwableProxy;
  private CallerData[] callerDataArray;
  private Marker marker;
  private Map<String, String> mdcPropertyMap;
  private LoggerRemoteView lrv;
  private long timeStamp;

  public static LoggingEventSDO build(ILoggingEvent le) {
    LoggingEventSDO ledo = new LoggingEventSDO();
    ledo.lrv = le.getLoggerRemoteView();
    ledo.threadName = le.getThreadName();
    ledo.level = (le.getLevel());
    ledo.message = (le.getMessage());
    ledo.argumentArray = (le.getArgumentArray());
    ledo.marker = le.getMarker();
    ledo.mdcPropertyMap = le.getMDCPropertyMap();
    ledo.setTimeStamp(le.getTimeStamp());
    ledo.throwableProxy = le.getThrowableProxy();
    return ledo;
  }

  public String getThreadName() {
    return threadName;
  }

  public Level getLevel() {
    return level;
  }
  
  public String getMessage() {
    return message;
  }

  public String getFormattedMessage() {
    if (formattedMessage != null) {
      return formattedMessage;
    }

    if (argumentArray != null) {
      formattedMessage = MessageFormatter.arrayFormat(message, argumentArray);
    } else {
      formattedMessage = message;
    }

    return formattedMessage;
  }

  public Object[] getArgumentArray() {
    return argumentArray;
  }

  public ThrowableProxy getThrowableProxy() {
    return throwableProxy;
  }

  public CallerData[] getCallerData() {
    return callerDataArray;
  }

  public Marker getMarker() {
    return marker;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeInt(level.levelInt);
    if (argumentArray != null) {
      int len = argumentArray.length;
      out.writeInt(len);
      for (int i = 0; i < argumentArray.length; i++) {
        if (argumentArray[i] != null) {
          out.writeObject(argumentArray[i].toString());
        } else {
          out.writeObject(NULL_ARGUMENT_ARRAY_ELEMENT);
        }
      }
    } else {
      out.writeInt(NULL_ARGUMENT_ARRAY);
    }

  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    int levelInt = in.readInt();
    level = Level.toLevel(levelInt);

    int argArrayLen = in.readInt();
    if (argArrayLen != NULL_ARGUMENT_ARRAY) {
      argumentArray = new String[argArrayLen];
      for (int i = 0; i < argArrayLen; i++) {
        Object val = in.readObject();
        if (!NULL_ARGUMENT_ARRAY_ELEMENT.equals(val)) {
          argumentArray[i] = val;
        }
      }
    }
  }

  public long getContextBirthTime() {
    return lrv.loggerContextView.getBirthTime();
  }

  public LoggerRemoteView getLoggerRemoteView() {
    return lrv;
  }

  public Map<String, String> getMDCPropertyMap() {
    return mdcPropertyMap;
  }

  public void prepareForDeferredProcessing() {
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result
        + ((threadName == null) ? 0 : threadName.hashCode());
    result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final LoggingEventSDO other = (LoggingEventSDO) obj;
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.message))
      return false;

    if (threadName == null) {
      if (other.threadName != null)
        return false;
    } else if (!threadName.equals(other.threadName))
      return false;
    if (timeStamp != other.timeStamp)
      return false;
    
    if (marker == null) {
      if (other.marker != null)
        return false;
    } else if (!marker.equals(other.marker))
      return false;
    
    if (mdcPropertyMap == null) {
      if (other.mdcPropertyMap != null)
        return false;
    } else if (!mdcPropertyMap.equals(other.mdcPropertyMap))
      return false;
    return true;
  }

  
}
