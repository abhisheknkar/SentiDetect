import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Font;


public class Frame1 {

	private JFrame frame;
	private JTextField textFieldNum1;
	private JTextField textFieldNum2;
	private JTextField textFieldOut;
	private JTextField textFieldOperator;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame1 window = new Frame1();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Frame1() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Abhishek's Calculator");
		
		textFieldNum1 = new JTextField();
		textFieldNum1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		textFieldNum1.setBounds(37, 36, 128, 50);
		frame.getContentPane().add(textFieldNum1);
		textFieldNum1.setColumns(10);
		textFieldNum1.setHorizontalAlignment(JTextField.CENTER);
		
		textFieldNum2 = new JTextField();
		textFieldNum2.setBounds(250, 36, 128, 50);
		frame.getContentPane().add(textFieldNum2);
		textFieldNum2.setColumns(10);
		textFieldNum2.setHorizontalAlignment(JTextField.CENTER);
		
		textFieldOut = new JTextField();
		textFieldOut.setColumns(10);
		textFieldOut.setBounds(147, 156, 128, 50);
		frame.getContentPane().add(textFieldOut);
		textFieldOut.setHorizontalAlignment(JTextField.CENTER);
		
		textFieldOperator = new JTextField();
		textFieldOperator.setBounds(177, 36, 57, 50);
		frame.getContentPane().add(textFieldOperator);
		textFieldOperator.setColumns(10);
		textFieldOperator.setHorizontalAlignment(JTextField.CENTER);
		
		JLabel lblOperand = new JLabel("Operand1");
		lblOperand.setBounds(74, 0, 74, 32);
		frame.getContentPane().add(lblOperand);
		
		JLabel lblOperand_1 = new JLabel("Operand2");
		lblOperand_1.setBounds(277, 0, 74, 32);
		frame.getContentPane().add(lblOperand_1);
		
		JLabel lblNewLabel = new JLabel("Operator");
		lblNewLabel.setBounds(177, 2, 67, 28);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblOutput = new JLabel("Output");
		lblOutput.setBounds(188, 211, 87, 31);
		frame.getContentPane().add(lblOutput);
		
		JButton buttonequals = new JButton("=");
		buttonequals.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double num1, num2, output=0;
				String operator;
				boolean invalidflag;

				
				try
				{
					num1 = Double.parseDouble(textFieldNum1.getText());
					num2 = Double.parseDouble(textFieldNum2.getText());
					operator = textFieldOperator.getText();
					invalidflag = false;

					switch (operator)
					{
						case "+":
							output = num1 + num2;
							break;
						case "-":
							output = num1 - num2;
							break;
						case "*":
							output = num1 * num2;
							break;
						case "/":
							if(num2 == 0) 
							{
								textFieldOut.setText("INVALID operation!");
								invalidflag = true;
								break;
							}
							output = num1 / num2;
							break;
						default:
							textFieldOut.setText("INVALID operation!");
							invalidflag = true;
							break;
					}
					if(!invalidflag) 
					{
						textFieldOut.setText(Double.toString(output));
					}
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(null, "Please enter valid operators and operands.");
				}
				
			}
		});
		buttonequals.setFont(new Font("Tahoma", Font.BOLD, 20));
		buttonequals.setBounds(185, 117, 59, 33);
		frame.getContentPane().add(buttonequals);
	}
}
