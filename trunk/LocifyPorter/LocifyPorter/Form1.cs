using System;

using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Threading;
using System.Net.Sockets;
using System.IO.Ports;
using System.Net;
using System.IO;
using System.Runtime.InteropServices;
using Microsoft.Win32;


namespace LocifyPorter
{
    public partial class Form1 : Form
    {
        public const string key = @"HKEY_CURRENT_USER\Software\Locify\LocifyPorter";


        public Form1()
        {
            InitializeComponent();
        }


        [DllImport("coredll.dll")]
        internal static extern IntPtr GetCapture();

        [DllImport("coredll.dll")]
        internal static extern IntPtr GetWindow(IntPtr hWnd, int uCmd);

        [DllImport("coredll.dll")]
        internal static extern UInt16 SendMessage(IntPtr hWnd, UInt16 msg,
            UInt16 wParam, UInt16 lParam);

       
        public enum InputMode {
            Spell = 0, T9 = 1, Numbers = 2, Text = 3}


        public static void SetInputMode(Control ctrl, InputMode mode)
        {
            int GW_CHILD = 5;
            UInt16 EM_SETINPUTMODE = 222;

            // Get the handle for the current control 
            ctrl.Capture = true;
            IntPtr h = GetCapture();
            ctrl.Capture = false;
            // Get the child window for the control 
            IntPtr hEditbox = GetWindow(h, GW_CHILD);
            // Set the input mode 
            SendMessage(hEditbox, EM_SETINPUTMODE, 0, (UInt16)mode);
        } 


        private string Com;
        private int Baud;

        string textToAdd = "";
        bool Running = false;
        bool ThreadOffTest = false;
        public static ManualResetEvent allDone = new ManualResetEvent(false);
        private Socket s;

        private void btnStartStop_Click(object sender, EventArgs e)
        {
            if (btnStartStop.Text == "Start")
            {
                Running = true;
                btnStartStop.Text = "Stop";
                Thread T = new Thread(new ThreadStart(ThreadGPS));
                T.Start();
            }
            else
            {
                Running = false;
                allDone.Set();
                s.Close();
                btnStartStop.Text = "Start";
            }
        }


        private void ThreadGPS()
        {
            ThreadOffTest = true;
            Byte[] ipAdress = { 127, 0, 0, 1 };
            IPAddress ip = new IPAddress(ipAdress);
            IPEndPoint ep = new IPEndPoint(ip, 20175);
            s = new Socket(ep.Address.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            s.Bind(ep);
            s.Listen(1000);
            while (Running)
            {
                allDone.Reset();
                textToAdd = "Waiting for Locify connection.";
                this.Invoke(new EventHandler(Update_label));

                s.BeginAccept(new AsyncCallback(AcceptCallback), s);
                allDone.WaitOne();
            }
            if (ThreadOffTest)
            {
                textToAdd = "Stopped waiting for Locify.";
                this.Invoke(new EventHandler(Update_label));
            }
            ThreadOffTest = false;
        }

        private void Update_label(object sender, EventArgs e)
        {
            txtLog.Text = DateTime.Now.ToString("HH:mm:ss ") + textToAdd + "\r\n" + txtLog.Text;
        }

        public void AcceptCallback(IAsyncResult ar)
        {
            if (Running)
            {
                Socket s = (Socket)ar.AsyncState;
                Socket h = s.EndAccept(ar);
                int bufSize = 10;
                try
                {
                    SerialPort sp = new SerialPort(Com, Baud);
                    try
                    {
                        sp.Open();
                        textToAdd = string.Format("{0} successfully opened.", Com);
                        this.Invoke(new EventHandler(Update_label));

                        while (Running)
                        {
                            byte[] buffer = new byte[bufSize];
                            sp.Read(buffer, 0, bufSize);
                            h.Send(buffer);
                        }
                    }
                    catch (SocketException)
                    {
                        textToAdd = "Locify midlet has left session.";
                        this.Invoke(new EventHandler(Update_label));
                    }
                    catch (Exception ex)
                    {
                        textToAdd = string.Format("Error on reading from {0}. ({1})", Com, ex.Message);
                        this.Invoke(new EventHandler(Update_label));
                    }
                }
                catch (IOException)
                {
                    textToAdd = string.Format("Error while opening port {0}.", Com);
                    this.Invoke(new EventHandler(Update_label));
                }
                finally
                {
                    //sp.Close();
                    h.Shutdown(SocketShutdown.Both);
                    h.Close();
                    textToAdd = string.Format("{0} successfully closed.", Com);
                    this.Invoke(new EventHandler(Update_label));
                    allDone.Set();  //zacnu znovu naslouchat
                }
            }
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            CreateRegTree();
            initRegistry();
            //btnRunIt.Visible = !(Registry.GetValue(Form1.key, "emulator", "none").ToString() == "none");
            //if ((int)Registry.GetValue(Form1.key, "auto", 1) == 1)
            //{
            //    btnStartStop_Click(sender, new EventArgs());
            //}

            SetInputMode(txtBaud, InputMode.Numbers);

            //Com = "COM4";
            //Baud = 57600;

            cbCom.SelectedItem = Com;
            txtBaud.Text = Baud.ToString();
        }

        private void cbCom_SelectedIndexChanged(object sender, EventArgs e)
        {
            Com = cbCom.Text;
        }

        private void txtBaud_TextChanged(object sender, EventArgs e)
        {
            if (txtBaud.Text != "")
            {
                Baud = int.Parse(txtBaud.Text);
            }
        }

        private void txtBaud_KeyPress(object sender, KeyPressEventArgs e)
        {
            e.Handled = !(char.IsNumber(e.KeyChar) || char.IsControl(e.KeyChar));
        }

        private void menuItem2_Click(object sender, EventArgs e)
        {
            ExitApp();
            Application.Exit();
        }

        private void menuItem1_Click(object sender, EventArgs e)
        {
            help form = new help();
            form.ShowDialog();
            form.Dispose();
        }

        private void Form1_Closing(object sender, CancelEventArgs e)
        {
            ExitApp();
            
        }

        private void ExitApp()
        {
            Registry.SetValue(Form1.key, "com", Com, RegistryValueKind.String);
            Registry.SetValue(Form1.key, "baud", Baud.ToString(), RegistryValueKind.String);
            if (btnStartStop.Text == "Stop")
            {
                Running = false;
                ThreadOffTest = false;
                allDone.Set();

                s.Close();
                btnStartStop.Text = "Start";
                Application.DoEvents();
            }
            //notifyIcon.Remove();
        }

        public static void CreateRegTree()
        {
            RegistryKey rk = Registry.CurrentUser.OpenSubKey("Software", true);
            RegistryKey rk2 = rk.CreateSubKey("Locify");
            rk.Close();
            rk = rk2.CreateSubKey("LocifyPorter");
            rk.Close();
            rk2.Close();
        }

        private void initRegistry()
        {
            Com = Registry.GetValue(Form1.key, "com", "COM4").ToString();
            Baud = int.Parse(Registry.GetValue(Form1.key, "baud", "9600").ToString());
        }
    }
}