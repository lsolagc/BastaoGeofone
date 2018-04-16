package br.com.gasi.bastogeofone;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InspecaoHistory#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InspecaoHistory extends Fragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private ListView mListView;
    private AlertDialog mOptionsDialog = null;

    public InspecaoHistory() {
        // Required empty public constructor
    }

    public static InspecaoHistory newInstance(String param1, String param2) {
        return new InspecaoHistory();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inspecao_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = view.findViewById(R.id.lv_InspHist);
        mListView.setLongClickable(true);
        mListView.setOnItemLongClickListener(this);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        createOptionsDialog(i);
        return false;
    }

    private void createOptionsDialog(final int itemLongClickedId){
        try{
            mOptionsDialog = new AlertDialog.Builder(getActivity()).create();
            View mView = getLayoutInflater().inflate(R.layout.opcoes_inspecao, null);
            Button btnDeleteInsp = mView.findViewById(R.id.btn_delete_inspecao);
            btnDeleteInsp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteInspecao(itemLongClickedId);
                }
            });
            mOptionsDialog.setView(mView);
            mOptionsDialog.setCancelable(true);
            mOptionsDialog.show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void deleteInspecao(int itemId) {
        //Todo: deletar tabela e entrada referentes à inspeção selecionada
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //Todo: abrir tela com os dados da inspeção
    }
}
