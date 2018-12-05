import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JFrame;

import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

import guybrush.view.Fenetre;
import io.netty.util.internal.SystemPropertyUtil;
import monkeys.MIRemote;

import guybrush.*;

////ARRET ALAPAGE 4 DU TP 4

public class Main implements MessageListener {

	private static Main instance;

	private int id = 0;
	
	private int myId = -1;

	private static Fenetre fenetre;
	

	// private static TopicSession topicSession;

	public static void main(String[] args) throws Exception {
		try {
			instance = new Main();

			fenetre = new Fenetre("Monkeys");
			fenetre.setBounds(new Rectangle(200, 100));
			fenetre.setVisible(true);
			fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			// TEST FERMETURE FENETRE
			fenetre.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					notifyDisconnect();
				}
			});

			MIRemote remoteMi = lookup();
			instance.subscribeTopic();
			remoteMi.subscribe("2");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see java.lang.Object#Object()
	 */
	public Main() {
		super();
	}

	// public void main() {
	// this.instance = new Main();
	// this.id = hashCode();
	// }

	private static MIRemote lookup() throws Exception {
		Properties properties = new Properties();

		// 1.3.3 Identification et recherche d’un composant distant distribué (JNDI)
		properties.setProperty("java.remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
		properties.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		properties.setProperty("remote.connections", "default");
		properties.setProperty("remote.connection.default.host", "localhost");
		properties.setProperty("remote.connection.default.port", "8080");
		properties.setProperty("remote.connection.default.username", "appManager");
		properties.setProperty("remote.connection.default.password", "Archos");

		// Créer une conf client a partir des propriétées précédemment remplies
		EJBClientConfiguration clientConf = new PropertiesBasedEJBClientConfiguration(properties);
		ConfigBasedEJBClientContextSelector clientContextSelector = new ConfigBasedEJBClientContextSelector(clientConf);
		EJBClientContext.setSelector(clientContextSelector);
		Context context = null;
		MIRemote remoteMi = null;

		try {
			context = new InitialContext(properties);
			remoteMi = (MIRemote) context.lookup("ejb:/TP3ServerEJB/MonkeyIsland!monkeys.MIRemote?stateful");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return remoteMi;
	}

	public void subscribeTopic() {

		java.util.Properties properties = new java.util.Properties();

		try {
			properties.load(getClass().getClassLoader().getResourceAsStream("META-INF/jndi-topic-client.properties"));

			Context context = new InitialContext(properties);

			// 2.1.3
			// TopicConnectionFactory topicConnFacto = (TopicConnectionFactory) context
			// .lookup("ejb:" + properties.getProperty("connectionFactoryURI"));
			TopicConnectionFactory topicConnFacto = (TopicConnectionFactory) context
					.lookup(properties.getProperty("connectionFactoryURI"));

			// Topic topic = (Topic) context.lookup("ejb:" +
			// properties.getProperty("topicURI"));
			Topic topic = (Topic) context.lookup(properties.getProperty("topicURI"));

			TopicConnection topicConn = topicConnFacto.createTopicConnection(
					properties.getProperty("java.naming.security.principal"),
					properties.getProperty("java.naming.security.credentials"));

			TopicSession topicSession = topicConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageConsumer messageCons = topicSession.createSharedDurableConsumer(topic,
					String.valueOf(instance.hashCode()));

			messageCons.setMessageListener(instance);
			topicConn.start();
			// return topicSession;
		} catch (Exception e) {
			e.printStackTrace();
			// return null;
		}

	}

	@Override
	public void onMessage(Message message) {
		//TODO
		System.out.println("Début onMessage()");
		// System.out.println(arg0.toString());
		// TODO Auto-generated method stub
		try {
			System.out.println("Message type : " + message.getJMSType());

			switch (message.getJMSType()) {
			
			case "map":
				System.out.println("Début onMessage(MAP)");
				int mapLength = ((StreamMessage) message).readInt();
				int[][] map = new int[mapLength][mapLength];
				for (int i = 0; i < mapLength; i++) {
					for (int j = 0; j < mapLength; j++) {
						map[i][j] = ((StreamMessage) message).readInt();
					}
				}
				fenetre.creationCarte(map);
				fenetre.repaint();
				break;
				
			case "Pirate" :
				System.out.println("Début onMessage(Pirate)");
				int id1 = message.getIntProperty("id");
				int x1 = message.getIntProperty("x");
				int y1 = message.getIntProperty("y");
				int energyLevel = message.getIntProperty("energy");
				String path = "img/Autres_Pirates.jpg";

				fenetre.ajoutPirate(id1, x1, y1, path, energyLevel);
			

				fenetre.repaint();
				break;
				
			case "YourPirate" :
				System.out.println("Début onMessage(Pirate)");
				int id42 = message.getIntProperty("id");
				myId = id42;
				int x42 = message.getIntProperty("x");
				int y42 = message.getIntProperty("y");
				int energyLevel42 = message.getIntProperty("energy");
				String path42 = "img/Mon_Pirate.png";

				fenetre.ajoutPirate(id42, x42, y42, path42, energyLevel42);
			

				fenetre.repaint();
				break;
				
			
			case "DeathPirate" :
				System.out.println("Début onMessage(DeathPirate)");
				int id2 = message.getIntProperty("id");
				fenetre.mortPirate(id2);
				fenetre.repaint();
				break;
				
			case "Singe" :
				System.out.println("Début onMessage(Singe)");
				int id3 = message.getIntProperty("id");
				int x3 = message.getIntProperty("x");
				int y3 = message.getIntProperty("y");
				fenetre.creationEMonkey(id, x3, y3);
				fenetre.repaint();
				break;
				
			case "Rhum" :
				System.out.println("Début onMessage(Rhum)");
				int x4 = message.getIntProperty("x");
				int y4 = message.getIntProperty("y");
				fenetre.creationRhum(x4, y4, true);
				fenetre.repaint();
				break;
				
			case "SuppressionPirate" :
				System.out.println("Début onMessage(SuppressionPirate)");
				int id5 = message.getIntProperty("id");
				fenetre.suppressionPirate(id5);
				fenetre.repaint();
				break;
				
			case "SuppressionPirates" :
				//TODO: remplir => avancer dans un tableau d'ID7
				System.out.println("Début onMessage(SuppressionPirateSSS)");
				break;
				
			case "Tresor" :
				System.out.println("Début onMessage(Tresor)");
				int x5 = message.getIntProperty("x");
				int y5 = message.getIntProperty("y");
				System.out.println("valeur"+x5+ "valuer2 "+y5);
				fenetre.creationTresor(x5, y5, true);
				fenetre.repaint();
				break;
				
			default :
				System.out.println("Message reçu non compris : " + message.getJMSType());
				break;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
			
			
			
			
////			MAP
//			if (message.getJMSType().equals("map")) {
//				System.out.println("Début onMessage(MAP)");
//				int mapLength = ((StreamMessage) message).readInt();
//				int[][] map = new int[mapLength][mapLength];
//				for (int i = 0; i < mapLength; i++) {
//					for (int j = 0; j < mapLength; j++) {
//						map[i][j] = ((StreamMessage) message).readInt();
//					}
//				}
//				fenetre.creationCarte(map);
//				fenetre.repaint();
//				
//				
////				PIRATE
//			} else if (message.getJMSType().equals("Pirate")) {
//				System.out.println("Début onMessage(Pirate)");
//				int id = message.getIntProperty("id");
//				int x = message.getIntProperty("x");
//				int y = message.getIntProperty("y");
//				int energyLevel = message.getIntProperty("energy");
//				String path = "img/Autres_Pirates.jpg";
//
//				fenetre.ajoutPirate(id, x, y, path, energyLevel);
//
//				fenetre.repaint();
//			}
//			//DEATHPIRATE
//			else if (message.getJMSType().equals("DeathPirate")) {
//				System.out.println("Début onMessage(DeathPirate)");
//				int id = message.getIntProperty("id");
//				fenetre.mortPirate(id);
//				fenetre.repaint();
//			}
////			SINGE
//			else if (message.getJMSType().equals("Singe")) {
//				System.out.println("Début onMessage(Singe)");
//				int id = message.getIntProperty("id");
//				int x = message.getIntProperty("x");
//				int y = message.getIntProperty("y");
//				fenetre.creationEMonkey(id, x, y);
//				fenetre.repaint();
//			}
////			RHUM
//			else if (message.getJMSType().equals("Rhum")) {
//				System.out.println("Début onMessage(Rhum)");
//				System.out.println(message.getIntProperty("x"));
//				int x = message.getIntProperty("x");
//				System.out.println(message.getIntProperty("y"));
//				int y = message.getIntProperty("y");
//				fenetre.creationRhum(x, y, true);
//				fenetre.repaint();
//			}
////			SUPPRESSION PIRATE
//			else if (message.getJMSType().equals("SuppressionPirate")) {
//				System.out.println("Début onMessage(SuppressionPirate)");
//				int id = message.getIntProperty("id");
//				fenetre.suppressionPirate(id);
//				fenetre.repaint();
//			}
//			//SUPPRESSION PIRATES
//			else if (message.getJMSType().equals("SuppressionPirates")) {
//				//TODO: remplir => avancer dans un tableau d'ID
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

	public static void notifyDisconnect() {
		System.out.println("NotifyDisconnect");
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}