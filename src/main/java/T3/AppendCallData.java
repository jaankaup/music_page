package T3;

import T3.DropBox.*;

public class AppendCallData {

  public AppendCallData() {}

  private UploadSessionArgs usa;
  private byte[] data;

  public void setUsa(UploadSessionArgs args) { this.usa = args; }
  public UploadSessionArgs getUsa() { return usa; }

  public void setData(byte[] data) { this.data = data; }
  public byte[] getData() { return data; }
}
