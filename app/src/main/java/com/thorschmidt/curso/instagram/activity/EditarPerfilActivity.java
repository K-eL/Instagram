package com.thorschmidt.curso.instagram.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thorschmidt.curso.instagram.R;
import com.thorschmidt.curso.instagram.model.Usuario;
import com.thorschmidt.curso.instagram.util.ConfiguracaoFirebase;
import com.thorschmidt.curso.instagram.util.Permissao;
import com.thorschmidt.curso.instagram.util.UsuarioFirebase;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilActivity extends AppCompatActivity {

    private static final int SELECAO_GALERIA = 200;
    private CircleImageView imageEditarPerfil;
    private TextView textAlterarFoto;
    private TextInputEditText editNomePerfil, editEmailPerfil;
    private Button buttonSalvarAlteracoes;
    private Usuario usuarioLogado;
    private StorageReference storageRef;
    private String identificadorUsuario;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        //Validar permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1 );

        //Configurações iniciais
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();

        //Configura toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Editar perfil");
        setSupportActionBar( toolbar );

        // configura um botão "Voltar"
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //inicializar componentes
        inicializarComponentes();

        //Recuperar dados do usuário
        getUserData();

        // configura os listeners
        setListeners();

    }

    private void getUserData() {
        FirebaseUser usuarioPerfil = UsuarioFirebase.getUsuarioAtual();
        editNomePerfil.setText( usuarioPerfil.getDisplayName().toUpperCase() );
        editEmailPerfil.setText( usuarioPerfil.getEmail() );

        Uri url = usuarioPerfil.getPhotoUrl();
        if( url != null ){
            Glide.with(EditarPerfilActivity.this)
                    .load( url )
                    .into( imageEditarPerfil );
        }else {
            imageEditarPerfil.setImageResource(R.drawable.avatar);
        }
    }

    private void setListeners() {
        //Salvar alterações do nome
        buttonSalvarAlteracoes.setOnClickListener(v -> {

            String nomeAtualizado = editNomePerfil.getText().toString();

            //atualizar nome no perfil
            UsuarioFirebase.atualizarNomeUsuario( nomeAtualizado );

            //Atualizar nome no banco de dados
            usuarioLogado.setNome( nomeAtualizado );
            usuarioLogado.atualizar();

            Toast.makeText(EditarPerfilActivity.this,
                    "Dados alterados com sucesso!",
                    Toast.LENGTH_SHORT).show();

        });

        //Alterar foto do usuario
        textAlterarFoto.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if( i.resolveActivity(getPackageManager()) != null ){
                startActivityForResult(i, SELECAO_GALERIA );
            }
        });
    }

    // resultado da recuperação da foto da galeria
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK ){
            Bitmap imagem = null;

            try {

                //Selecao apenas da galeria
                switch ( requestCode ){
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada );
                        break;
                }

                //Caso tenha sido escolhido uma imagem
                if ( imagem != null ){

                    //Configura imagem na tela
                    imageEditarPerfil.setImageBitmap( imagem );

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no firebase
                    StorageReference imagemRef = storageRef
                            .child("imagens")
                            .child("perfil")
                            .child( identificadorUsuario + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //Recuperar local da foto
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            atualizarFotoUsuario( uri );
                                        }
                                    });
                                }
                            }
                        }
                    });


                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    private void atualizarFotoUsuario(Uri url){

        //Atualizar foto no perfil
        UsuarioFirebase.atualizarFotoUsuario( url );

        //Atualizar foto no Firebase
        usuarioLogado.setCaminhoFoto( url.toString() );
        usuarioLogado.atualizar();

        Toast.makeText(EditarPerfilActivity.this,
                "Sua foto foi atualizada!",
                Toast.LENGTH_SHORT).show();

    }

    private void inicializarComponentes(){

        imageEditarPerfil      = findViewById(R.id.imageEditarPerfil);
        textAlterarFoto        = findViewById(R.id.textAlterarFoto);
        editNomePerfil         = findViewById(R.id.editNomePerfil);
        editEmailPerfil        = findViewById(R.id.editEmailPerfil);
        buttonSalvarAlteracoes = findViewById(R.id.buttonSalvarAlteracoes);
        editEmailPerfil.setFocusable(false);

    }

    // sempre chamado quando pressionar o botão voltar
    @Override
    public boolean onSupportNavigateUp() {
        // fecha a activity
        finish();
        // evita voltar para o main activity (definido no manifest)
        return false;

    }
}
