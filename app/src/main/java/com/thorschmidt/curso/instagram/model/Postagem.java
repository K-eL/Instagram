package com.thorschmidt.curso.instagram.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.thorschmidt.curso.instagram.util.ConfiguracaoFirebase;
import com.thorschmidt.curso.instagram.util.UsuarioFirebase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Postagem implements Serializable {

    /*
     * Modelo de postagem
     * postagens
     *  <id_usuario>
     *      <id_postagem_firebase>
     *          descricao
     *          caminhoFoto
     *          idUsuario
     *
     * */

    private String id;
    private String idUsuario;
    private String descricao;
    private String caminhoFoto;

    public Postagem() {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference postagemRef = firebaseRef.child("postagens");
        // get the ID to use with other registers
        String idPostagem = postagemRef.push().getKey();
        setId( idPostagem );

    }

    public boolean salvar(DataSnapshot seguidoresSnapshot){

        Map objeto = new HashMap();
        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();

        //ReferÃªncia para postagem
        String combinacaoId = "/" + getIdUsuario() + "/" + getId();
        objeto.put("/postagens" + combinacaoId, this );

        //ReferÃªncia para postagem
        for( DataSnapshot seguidores: seguidoresSnapshot.getChildren() ){

            /*
            + feed
              + id_seguidor<jose renato>
                + id_postagem <01>
                    postagem< por jamilton>
           */
            String idSeguidor = seguidores.getKey();

            //Monta objeto para salvar
            HashMap<String, Object> dadosSeguidor = new HashMap<>();
            dadosSeguidor.put("fotoPostagem", getCaminhoFoto() );
            dadosSeguidor.put("descricao", getDescricao() );
            dadosSeguidor.put("id", getId() );
            dadosSeguidor.put("nomeUsuario", usuarioLogado.getNome() );
            dadosSeguidor.put("fotoUsuario", usuarioLogado.getCaminhoFoto() );

            String idsAtualizacao = "/" + idSeguidor + "/" + getId();
            objeto.put("/feed" + idsAtualizacao, dadosSeguidor );

        }

        firebaseRef.updateChildren( objeto );
        return true;

    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    private String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }
}
