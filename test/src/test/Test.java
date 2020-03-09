package test;


	public class Test implements Runnable {
	    int number = 10;

	    public void firstMethod() throws Exception {
	        synchronized (this) {
	            number += 100;
	            System.out.println(number);
	        }
	    }

	    public void secondMethod() throws Exception {
	        synchronized (this) {
	            
	           // Thread.sleep(2000);
	           this.wait(2000);
	            number *= 200;
	          
	        }
	    }

	    @Override
	    public void run() {
	        try {
	            firstMethod();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    public static void main(String[] args) throws Exception {
	        Test threadTest = new Test();
	        Thread thread = new Thread(threadTest);
	        thread.start();
	        threadTest.secondMethod();
	    }
}
