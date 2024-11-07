  //Essa é a primeira versão da junção de códigos que une a captura de um BPM com o seu registro na Firebase
  //Feito por Eduardo Paziani 11/09/2024
  //Aproveitem (:

  //inclusão das bibliotecas úteis para o projeto de esp com conexão à internet e à base de dados
  #include<WiFi.h>
  #include<Firebase_ESP_Client.h>
  #include "addons/TokenHelper.h"
  #include "addons/RTDBHelper.h"

  //inclusão da biblioteca para fazer o MAX30100 medir os batimentos cardíacos e a oxigenação do sangue
  #include <PulseSensorPlayground.h>     // Includes the PulseSensorPlayground Library.  

  //definição dos parâmetros úteis para a conexão
  #define WIFI_SSID "NOME_DA_REDE"                                                           //  ----> declaração do nome da rede a conectar
  #define WIFI_PASSWORD "SENHA_DA_REDE"                                                        //  ----> declaração da senha da rede a conectar
  #define API_KEY "CHAVE_DA_API_DO_SEU_PROJETO_NA_FIREBASE"                               //  ----> conexão da API KEY da Firebase com a esp  
  #define DATABASE_URL "SUA_URL_DA_REALTIME_DATABASE"       //  ----> definição de qual endereço a esp deve enviar os dados
  // Substitua pelo ID real do usuário ao qual você deseja salvar os dados
  #define USER_ID "UID_DO_SEU_USUÁRIO_CADASTRADO" 

  //  Variables
  const int PulseWire = 19;       // PulseSensor PURPLE WIRE connected to ANALOG PIN 0 -- No caso o pino GPIO19 vai ser utilizado
  //const int LED = LED_BUILTIN;          // The on-board Arduino LED, close to PIN 13.
  int Threshold = 550;           // Determine which Signal to "count as a beat" and which to ignore.
                                // Use the "Gettting Started Project" to fine-tune Threshold Value beyond default setting.
                                // Otherwise leave the default "550" value. 
                                
  PulseSensorPlayground pulseSensor;  // Creates an instance of the PulseSensorPlayground object called "pulseSensor"

  //definição das portas do esp que vai ler o(s) sensor(es)
  //#define MAX30100_PIN 19        //GPIO14 (exemplo)

  int myBPM = 0;

  //Criação dos objetos Firebase
  FirebaseData fbdo;
  FirebaseAuth auth;
  FirebaseConfig config;


  unsigned long sendDataPrevMillis = 0;
  bool signupOK = false;
  int ldrData = 0;
  float voltage = 0.0;


  void setup() {
    //iniciar o módulo e a sua conexão com o Wi-FI
    Serial.begin(115200);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Conecting to Wi-Fi");
    while(WiFi.status() != WL_CONNECTED){
      Serial.print(".");
      delay(15000);
    }
    Serial.println("");
    Serial.print("Connected with IP: ");
    Serial.println(WiFi.localIP());
  Serial.println("");

  //conexão com a Firebase
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  // Configura o token JWT gerado a partir do JSON da conta de serviço
  config.signer.tokens.legacy_token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJmaXJlYmFzZS1hZG1pbnNkay00cXFiY0BmaW5hbC01OGI3OS5pYW0uZ3NlcnZpY2VhY2NvdW50LmNvbSIsInN1YiI6ImZpcmViYXNlLWFkbWluc2RrLTRxcWJjQGZpbmFsLTU4Yjc5LmlhbS5nc2VydmljZWFjY291bnQuY29tIiwiYXVkIjoiaHR0cHM6Ly9pZGVudGl0eXRvb2xraXQuZ29vZ2xlYXBpcy5jb20vZ29vZ2xlLmlkZW50aXR5LmlkZW50aXR5dG9vbGtpdC52MS5JZGVudGl0eVRvb2xraXQiLCJpYXQiOjE3MzA4NTgyNzYsImV4cCI6MTczMDg2MTg3Nn0.fPpg6QIRjx-CQCKTDnNDFYIf5LwAwSfjqktUpn3SPnGBZoIESPzwC7QNw59QjQxXUFL-QL-Opd0QqMHXBvTpYPuEGSUGGTdVZ8KjDVKAes7A9gTovFM4fZSikGFNMLg9utzzyB5H_vkj-kfnjM5dfJXA3Q_opljYBDzIVE5ZyXNkprC9Ah1TQPs39xZqf_1EWduNoeXJj1LUj50OqO2x4Lqx0v7yyljU4CQFF-zUZOtsEifcQUGBZZ9_cAkr369SxygboG7nAkfjOyoKGqnOP-Nht68CHozSUyKq9HK2Z3C4yM8vVbgvKTrxNurt_HLwDQeZ2UwchDASkP3tbl7Qgw";

    config.token_status_callback = tokenStatusCallback;
    Firebase.begin(&config, &auth);
    Firebase.reconnectWiFi(true);

    // Configure the PulseSensor object, by assigning our variables to it. 
    pulseSensor.analogInput(PulseWire);   
    pulseSensor.blinkOnPulse(LED);       //auto-magically blink Arduino's LED with heartbeat.
    pulseSensor.setThreshold(Threshold);   

    // Double-check the "pulseSensor" object was created and "began" seeing a signal. 
    if (pulseSensor.begin()) {
      Serial.println("We created a pulseSensor Object !");  //This prints one time at Arduino power-up,  or on Arduino reset.  
    }
  }

  void loop() {
    
  if (pulseSensor.sawStartOfBeat()) {            // Constantly test to see if "a beat happened".
    int myBPM = pulseSensor.getBeatsPerMinute();  // Calls function on our pulseSensor object that returns BPM as an "int".
                                                // "myBPM" hold this BPM value now. 
    Serial.println("♥  A HeartBeat Happened ! "); // If test is "true", print a message "a heartbeat happened".
    Serial.print("BPM: ");                        // Print phrase "BPM: " 
    Serial.println(myBPM);                        // Print the value inside of myBPM. 

    delay(20);                    // considered best practice in a simple sketch.

    //sending data do the database periodically (a cada 5 segundos ou ao recomeçar o loop)
    if(Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0)){
      sendDataPrevMillis = millis();

      // to store sensor data to our realtime database
      ldrData = analogRead(PulseWire);                      //aqui o dado que está sendo lido é referente ao dado recebido do MAX30100 que está no GPIO14
      voltage = (float)analogReadMilliVolts(PulseWire)/1000;    //aqui a leitura da voltagem nesse pino está sendo feita e salvada
    }
    // Salva bpm_data no caminho específico do usuário
    String bpmPath = "SharedData/" + String(USER_ID) + "/sensor_data/bpm_data";
    if(Firebase.RTDB.setInt(&fbdo, bpmPath.c_str(), myBPM)){
      Serial.println();
      Serial.print(myBPM);
      Serial.print("  -  succesfully saved to: " + fbdo.dataPath());
      Serial.println(" (" + fbdo.dataType() +")");
    }else{
      Serial.println("FAILED: " + fbdo.errorReason());
    }

    // Salva voltage no caminho específico do usuário
    String voltagePath = "SharedData/" + String(USER_ID) + "/sensor_data/voltage";
    if(Firebase.RTDB.setFloat(&fbdo, voltagePath.c_str(), voltage)){
      Serial.println();
      Serial.print(voltage);
      Serial.print("  -  succesfully saved to: " + fbdo.dataPath());
      Serial.println(" (" + fbdo.dataType() +")");
    }else{
      Serial.println("FAILED: " + fbdo.errorReason());
    }
    delay(2000);
  }
  }
