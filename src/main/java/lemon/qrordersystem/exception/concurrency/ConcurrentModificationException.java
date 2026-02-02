package lemon.qrordersystem.exception.concurrency;

public class ConcurrentModificationException extends RuntimeException {
  public ConcurrentModificationException(String message) {
    super(message);
  }
}
