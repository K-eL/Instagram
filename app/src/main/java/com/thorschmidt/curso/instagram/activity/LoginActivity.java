package com.thorschmidt.curso.instagram.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.thorschmidt.curso.instagram.R;
import com.thorschmidt.curso.instagram.model.Usuario;
import com.thorschmidt.curso.instagram.util.ConfiguracaoFirebase;

public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private Button botaoEntrar;
    private ProgressBar progressBar;

    private Usuario usuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        verificarUsuarioLogado();
        inicializarComponentes();

        //Fazer login do usuario
        progressBar.setVisibility( View.GONE );
        botaoEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textoEmail = campoEmail.getText().toString();
                String textosenha = campoSenha.getText().toString();

                if( !textoEmail.isEmpty() ){
                    if( !textosenha.isEmpty() ){

                        usuario = new Usuario();
                        usuario.setEmail( textoEmail );
                        usuario.setSenha( textosenha );
                        validarLogin( usuario );

                    }else{
                        Toast.makeText(LoginActivity.this,
                                "Preencha a senha!",
                                Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(LoginActivity.this,
                            "Preencha o e-mail!",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void verificarUsuarioLogado(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if( autenticacao.getCurrentUser() != null ){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    private void validarLogin(Usuario usuario){

        progressBar.setVisibility( View.VISIBLE );
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if ( task.isSuccessful() ){
                    progressBar.setVisibility( View.GONE );
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }else {
                    Toast.makeText(LoginActivity.this,
                            "Erro ao fazer login",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility( View.GONE );
                }
            }
        });
    }

    public void abrirCadastro(View view){
        Intent i = new Intent(LoginActivity.this, CadastroActivity.class);
        startActivity( i );
    }

    private void inicializarComponentes(){

        campoEmail   = findViewById(R.id.editLoginEmail);
        campoSenha   = findViewById(R.id.editLoginSenha);
        botaoEntrar  = findViewById(R.id.buttonEntrar);
        progressBar  = findViewById(R.id.progressLogin);

        campoEmail.requestFocus();


    }

}

