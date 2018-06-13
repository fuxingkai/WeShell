package cn.jufuns.ws.downloader.bizs;

class DLException extends Exception {

    private static final long serialVersionUID = -6609130935466031597L;

    DLException() {
        super();
    }

    DLException(String detailMessage) {
        super(detailMessage);
    }

    DLException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    DLException(Throwable throwable) {
        super(throwable);
    }
}
