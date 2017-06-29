package org.myframe.https;

public interface HttpsCb {
	void onResponse(String data,RequestBean rb);
	void onError(String error,RequestBean rb);
}
