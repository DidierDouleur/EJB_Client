import java.awt.Rectangle;
import java.awt.event.KeyEvent;
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
import guybrush.view.GameObserver;
import io.netty.util.internal.SystemPropertyUtil;
import monkeys.MIRemote;

import guybrush.*;

////ARRET ALAPAGE 4 DU TP 4

public class Main implements MessageListener, GameObserver {

	private static Main instance;

	private int id = 0;

	private static int myId = -1;

	private static Fenetre fenetre;

	private static MIRemote remoteMi;

	// private static TopicSession topicSession;

	public static void main(String[] args) throws Exception {
		try {
			instance = new Main();

			fenetre = new Fenetre("Monkeys");
			fenetre.setBounds(new Rectangle(200, 100));
			fenetre.setVisible(true);
			fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			fenetre.addObserver(instance);
			// TEST FERMETURE FENETRE
			
			fenetre.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					instance.notifyDisconnect();
				}
			});
			
			// TODO ajouter evenement sur appuit touche

			// MIRemote remoteMi = lookup();
			remoteMi = lookup();
			instance.subscribeTopic();
			remoteMi.subscribe("2");

			// Exemple de déplacement
			// remoteMi.move(8, 8, myId);

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


	private static MIRemote lookup() throws Exception {
		Properties properties = new Properties();

		// 1.3.3 Identification et recherche d’un composant distant distribué (JNDI)
		properties.setProperty("java.remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
		properties.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		properties.setProperty("remote.connections", "default");
		properties.setProperty("remote.connection.default.host", "localhost");
		properties.setProperty("remote.connection.default.port", "8080");
		
		//ID CONNECTION SERVER
		properties.setProperty("remote.connection.default.username", "appManager");
		//MDP CONNECTION SERVER
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
		// TODO
		// System.out.println(arg0.toString());
		// TODO Auto-generated method stub
		try {
			switch (message.getJMSType()) {

			case "map":
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

			case "YourID":
				int id1 = message.getIntProperty("id");
				if(myId == -1) {
					myId = id1;
				}
				break;

			case "Pirate":
				int id42 = message.getIntProperty("id");
				String image = "img/Autres_Pirates.jpg";
				if (myId == id42) {
					image = "img/Mon_Pirate.png";
				}
				int x42 = message.getIntProperty("x");
				int y42 = message.getIntProperty("y");
				int energyLevel42 = message.getIntProperty("energy");

				fenetre.ajoutPirate(id42, x42, y42, image, energyLevel42);
				fenetre.repaint();
				break;

			case "DeathPirate":
				int id2 = message.getIntProperty("id");
				fenetre.mortPirate(id2);
				fenetre.repaint();
				break;

			case "Singe":
				int id3 = message.getIntProperty("id");
				int x3 = message.getIntProperty("x");
				int y3 = message.getIntProperty("y");
				fenetre.creationEMonkey(id, x3, y3);
				fenetre.repaint();
				break;

			case "Rhum":
				int x4 = message.getIntProperty("x");
				int y4 = message.getIntProperty("y");
				fenetre.creationRhum(x4, y4, true);
				fenetre.repaint();
				break;

			case "SuppressionPirate":
				int id5 = message.getIntProperty("id");
				fenetre.suppressionPirate(id5);
				fenetre.repaint();
				break;

			case "Tresor":
				int x5 = message.getIntProperty("x");
				int y5 = message.getIntProperty("y");
				fenetre.creationTresor(x5, y5, false);
				fenetre.repaint();
				break;

			case "AllElements":
			
				int size = message.getIntProperty("size");
				fenetre.removeEMonkeys();
				fenetre.removeRhums();
				for (int i = 0; i < size; i++) {
					
					int id = message.getIntProperty("id" + i);
					int x = message.getIntProperty("x" + i);
					int y = message.getIntProperty("y" + i);
					int energy = message.getIntProperty("energy" + i);
					String type = message.getStringProperty("type"+i);
					boolean state = message.getBooleanProperty("state"+i);
					switch(type) {
					case "PIRATE":
						fenetre.suppressionPirate(id);
						if(id == myId) {
							fenetre.ajoutPirate(id, x, y, "img/Mon_Pirate.png", energy);
						}else {
							fenetre.ajoutPirate(id, x, y, "img/Autres_Pirates.jpg", energy);
						}
						if(!state) {
							fenetre.mortPirate(id);
						}
						break;
					case "MONKEY" :
						fenetre.creationEMonkey(id, x, y);
						break;
					case "RHUM" :
						fenetre.creationRhum(x, y, state);
					
					default :
						System.out.println("Type inconnu");
					}
				}

				break;

			default:
				System.out.println("Message reçu non compris : " + message.getJMSType());
				break;

			}
			fenetre.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void notifyDisconnect() {
		// TODO envoyer un message au serveur avec notre identifiant de pirate (afin que
		// le serveur le supprime de la partie)
		this.remoteMi.disconnect(String.valueOf(myId));
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notifyMove(int arg0, int arg1) {
		this.remoteMi.move(arg0, arg1, this.myId);		
	}

}