package io.openvidu.server.streaming;

import org.kurento.client.HubPort;
import org.kurento.client.MediaType;
import org.kurento.client.Composite;
import org.kurento.client.RtpEndpoint;

import io.openvidu.server.kurento.core.KurentoParticipant;
import io.openvidu.server.kurento.core.KurentoSession;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingManager {
    private final Logger log = LoggerFactory.getLogger(StreamingManager.class);
    private String serverHost = "wowza.councilbox.com";

    public String startStreamToWowza(KurentoSession session, KurentoParticipant participant) {

        int audioPort = 9996;
        int videoPort;

        /* if (!(freePorts.isEmpty())) {
            audioPort = freePorts.get(0);
            freePorts.remove(0);
        } else {
            audioPort += wowzaStreams * 4;
        }

        wowzaStreams++; */
        videoPort = audioPort + 2;
        String wowzaAddress = this.serverHost;

        String sdpContent = "v=0\n" +
                "o=- 0 3641150070 IN IP4 " + wowzaAddress + "\n" +
                "s=Wowza Media Server\n" +
                "c=IN IP4 " + wowzaAddress + "\n" +
                "t=0 0\n" +
                "m=audio " + audioPort + " RTP/AVP 14\n" +
                "a=rtpmap:14 MPA/90000\n" +
                "a=ssrc:3610119879 cname:user3527564918@host-d993465d\n" +
                "m=video " + videoPort + " RTP/AVP 101\n" +
                "a=rtpmap:101 H264/90000\n" +
                "a=ssrc:276641451 cname:user3527564918@host-d993465d";

        String filename = null;
        try {
            filename = createTempFile(session.getSessionId(), sdpContent);
            //accessSSH(filename, 0, session.getName());
            
            session.setComposite(new Composite.Builder(session.getPipeline()).build());

            HubPort hubPort = new HubPort.Builder(session.getComposite()).build();
            ConcurrentHashMap<String, HubPort> hPorts = session.gethPorts();
            hPorts.put(participant.getFinalUserId(), hubPort);
            session.sethPorts(hPorts);
            /*if(participant.getPublisher() == null) {
                throw new Exception(participant.toString());
            };*/
            (participant.getPublisher()).connect(hubPort, MediaType.AUDIO);
            session.setHubPortOut(new HubPort.Builder(session.getComposite()).build());

            RtpEndpoint rtpEndpoint = new
                    RtpEndpoint.Builder(session.getPipeline()).build();

            participant.getPublisher().connect(rtpEndpoint, MediaType.VIDEO);

            session.getHubPortOut().connect(rtpEndpoint, MediaType.AUDIO);


            rtpEndpoint.processOffer(sdpContent);
            session.setWowzaEndpoint(rtpEndpoint);
            session.setWowzaPort(audioPort);
            //System.out.println("\nSTREAMING A WOWZA, link para verlo:rtmp://streaminggalicia.com:1935/Kurento/" + filename + "\n");
            // manageRecording(filename,false);
        } catch (IOException e) {
            // TODO Auto-generated catch block
        	log.warn("Exception on accessSSH of startstreamtowowza");
            e.printStackTrace();
        }
        return filename;
    }

    // Creates a temporary file that will be deleted on JVM exit.
    private static String createTempFile(String filename, String content) throws IOException {
        // Since Java 1.7 Files and Path API simplify operations on files
        String tDir = System.getProperty("java.io.tmpdir");
        //try{
        // Path tempFile = Paths.get(tDir + filename + ".cbx");
        //System.out.println(Files.deleteIfExists(tempFile));
        //}catch(IOException e){
        //e.printStackTrace();
        //}

        //    	String path = tdir + File.separator + filename+".cbx";
        Path path = Paths.get(tDir + File.separator + filename + ".cbx");
        File file = path.toFile();
        // writing sample data
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return file.getAbsolutePath();
    }

    /* public String createWowzaFlow(String participantId){
    	
    	KurentoParticipant participant = getParticipant(participantId);
        String name = participant.getName();
        Room room = participant.getRoom();
        
    	String filename = null;
        
    	if (participant.isCreator() && !room.wasRecording() && !room.isCreatorRejoining() && !room.isPresenterRejoining()){
            filename = startStreamToWowza(room, participant);
    	}	
        else {
        	if (room.isPresenterRejoining()) {
        		participant.getPublisher().connect(room.getWowzaEndpoint(), MediaType.VIDEO);
        		room.setPresenterRejoining(false);
        	}
        	try{
        		HubPort hubPort = new HubPort.Builder(room.getComposite()).build();
        		participant.getPublisher().connect(hubPort, MediaType.AUDIO);
        		ConcurrentHashMap<String, HubPort> hubPorts = room.gethPorts();
        		hubPorts.put(participantId, hubPort);
        		room.sethPorts(hubPorts);
        	}catch(Exception ex){
        		log.error("Error in generate composite with participantId={}", participantId);
        	}
        }

        
    	return filename;
    } */

}