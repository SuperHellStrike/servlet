import java.awt.Point;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class Rotator extends Thread {
	
	private LinkedList<Point> objects;
	private Semaphore sem;
	
	public Rotator(LinkedList<Point> objects, Semaphore sem) {
		this.objects = objects;
		this.sem = sem;
	}
	
	@Override
	public void run() {
		
		while(!isInterrupted()) {
			try {
				sem.acquire();
				
				for(Point p : objects) {
					if(p.x < 400)
						p.x += 5;
					else
						p.x = 0;
				}
				
				sem.release();
				sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

@WebServlet("/Serv")
public class Serv extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private LinkedList<Point> objects = new LinkedList<>();
	private Semaphore sem = new Semaphore(1);
	private Rotator r;
	
    public Serv() {
        super();
        r = new Rotator(objects, sem);
        r.start();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DataOutputStream dos = new DataOutputStream(response.getOutputStream());
		
		String command = request.getParameter("command");
		if(command == null) {
			dos.writeInt(-2);
		}
		else if(command.compareToIgnoreCase("len") == 0) {
			try {
				sem.acquire();
				dos.writeInt(objects.size());
				sem.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		else if(command.compareToIgnoreCase("get") == 0) {
			String iStr = request.getParameter("index");
			if(iStr == null) {
				dos.writeInt(-1);
			}
			else {
				try {
					int i = Integer.parseInt(iStr);
					sem.acquire();
					Point p = objects.get(i);
					sem.release();
					dos.writeInt(p.x);
					dos.writeInt(p.y);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		else if(command.compareToIgnoreCase("del") == 0) {
			String iStr = request.getParameter("index");
			if(iStr == null) {
				dos.writeInt(-1);
			}
			else {
				try {
					int i = Integer.parseInt(iStr);
					sem.acquire();
					objects.remove(i);
					sem.release();
					dos.writeInt(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		else if(command.compareToIgnoreCase("all") == 0) {
			try {
				sem.acquire();
				for(Point p : objects) {
					dos.writeInt(p.x);
					dos.writeInt(p.y);
				}
				sem.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else if(command.compareToIgnoreCase("add") == 0) {
			String xStr = request.getParameter("x");
			String yStr = request.getParameter("y");
			if(xStr == null || yStr == null) {
				dos.writeInt(-2);
			}
			else {
				try {
					sem.acquire();
					objects.add(new Point(Integer.parseInt(xStr), Integer.parseInt(yStr)));
					sem.release();
					dos.writeInt(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			dos.writeInt(-1);
		}
		
		dos.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
