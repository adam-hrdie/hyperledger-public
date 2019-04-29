package hm.beans;

import java.io.Serializable;
import java.util.Arrays;

import com.google.gson.annotations.SerializedName;

public class MessageBean implements Serializable {

   private static final long serialVersionUID = 1L;

   @SerializedName(value = "id", alternate = { "Id" })
   private String id;

   @SerializedName(value = "type", alternate = { "Type" })
   private int type;

   @SerializedName(value = "timestamp", alternate = { "Timestamp" })
   private long timestamp;

   @SerializedName(value = "value", alternate = { "Value" })
   private byte[] value;

   public MessageBean(final String uuid, final int type, final byte[] value) {
      setId(uuid);
      setType(type);
      setTimestamp(1); // dummy until we ch  ange chaincode to accept int64
      setValue(value);
   }

   public static MessageBean newInstance(final String uuid, final int msgType, final byte[] bytes) {
      return new MessageBean(uuid, msgType, bytes);
   }

   public String getId() {
      return id;
   }

   public void setId(final String id) {
      this.id = id;
   }

   public int getType() {
      return type;
   }

   public void setType(final int type) {
      this.type = type;
   }

   public long getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(final long timestamp) {
      this.timestamp = timestamp;
   }

   public byte[] getValue() {
      return value;
   }

   public void setValue(final byte[] value) {
      this.value = value;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
      result = prime * result + type;
      result = prime * result + Arrays.hashCode(value);
      return result;
   }

   @Override
   public boolean equals(final Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final MessageBean other = (MessageBean) obj;
      if (id == null) {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      if (timestamp != other.timestamp)
         return false;
      if (type != other.type)
         return false;
      if (!Arrays.equals(value, other.value))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "MessageBean [" + "id='" + id + '\'' + ", type=" + type + ", timestamp=" + timestamp + ", value=" + new String(value) + ']';
   }
}
