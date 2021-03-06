package chatclient.lib;

import java.net.InetAddress;

import chatclient.log.Log;
import chatclient.log.LogType;

@SuppressWarnings("serial") 
class UnreachableIPException extends Exception {
	UnreachableIPException(InetAddress ip){
		Log.log(new String[] {
			ip.getHostAddress()
		}, LogType.UNREACHABLE_IP);
	}
}
