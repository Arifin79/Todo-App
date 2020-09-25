package com.arifin.todoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

//    buat variable database referance yang akan di isi oleh database firebase
    private lateinit var databaseRef : DatabaseReference

//    variable cekdata dibuat untuk read
    private lateinit var cekData : DatabaseReference

//    untuk memantau perubahan database
    private lateinit var readDataListener: ValueEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseRef= FirebaseDatabase.getInstance().reference

        btn_tambah.setOnClickListener {
//            ambil text dari edit text
            val nama = input_nama.text.toString()
            if (nama.isBlank()){
                toastData("Kolom Nama Harus Di isi")
            } else {
                tambahData(nama)
            }
        }

        btn_hapus.setOnClickListener {
            val nama = input_nama.text.toString()

            if (nama.isBlank()){
                toastData("Kolom Kalimat Harus Di isi")
            } else{
                hapusData(nama)
            }
        }

        btn_update.setOnClickListener {
            val kalimatAwal = input_nama.text.toString()
            val kalimatUpdate = edit_nama.text.toString()
            if (kalimatAwal.isBlank() || kalimatUpdate.isBlank()){
                toastData("kolom tidak boleh kosong")
            } else{
                updateData(kalimatAwal, kalimatUpdate)
            }
        }

        cekDataKalimat()

    }

    private fun updateData(kalimatAwal: String, kalimatUpdate: String) {
        val dataUpdate = HashMap<String, Any>()
        dataUpdate["Nama"] = kalimatUpdate

        val dataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount > 0){
                    databaseRef.child("Daftar Nama")
                        .child(kalimatAwal)
                        .updateChildren(dataUpdate)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) toastData("Data Berhasil Diupdate")
                        }
                } else {
                    toastData("Data Yang Di Tuju Tidak Ada Di Dalam Database")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        val dataAsal = databaseRef.child("Daftar Nama")
            .child(kalimatAwal)
        dataAsal.addListenerForSingleValueEvent(dataListener)
    }

    private fun hapusData(nama: String) {
//        membuat listener data firebase
        val dataListener = object : ValueEventListener {
            //untuk mengetahui aktivitas data
//            seperti penambahan, pengurangan, dan perubahan data
            override fun onDataChange(snapshot: DataSnapshot) {
//                snapshot.childrenCount untuk mengetahui banyak data yang ada di dalam tabel daftar nama
                if (snapshot.childrenCount > 0){
                    databaseRef.child("Daftar Nama").child(nama)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) toastData("$nama telah dihapus")
                        }
                } else {
                    toastData("Tidak ada data $nama")
                }

            }

            override fun onCancelled(p0: DatabaseError) {
               toastData("tidak bisa menghapus data tersebut")
            }

        }
//      untuk menghapus data, kita perlu cek data yang ada di dalam tabel daftar nama
        val cekData = databaseRef.child("Daftar Nama")
            .child(nama)
//        addValueEventListener itu menjalankan listener harus terus menerus selama data yang di inputkan
//        sedangkan addListenerForSingleValueEvent itu di jalankan sekali saja
        cekData.addListenerForSingleValueEvent(dataListener)
    }

    //untuk get data dari firebase
    private fun cekDataKalimat() {
        readDataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount > 0){
                    var textData = ""
                    for (data in snapshot.children) {
                        val nilai = data.getValue(ModelNama::class.java) as ModelNama
                        textData += "${nilai.Nama} \n"
                    }

                    txt_nama.text = textData
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        }

//    cekdata menuju ke database firebase di tabel "Daftar Nama"
        cekData= databaseRef.child("Daftar Nama")
//    addValueEventListener di gunakan untuk memantau perubahan database di tabel daftar nama
        cekData.addValueEventListener(readDataListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        cekData.removeEventListener(readDataListener)
    }

    private fun tambahData(nama: String) {
        val data = HashMap<String, Any>()
        data["Nama"] = nama

//        logika penambahan data yaitu cek terlebih dahulu data
//        kemudian tambahkan data jika data belum ada
        val dataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

//                snapshot childrenCount ini menghitung jumblah data
//                jika data kurang dari satu maka pasti tidak ada data jadi tambah data

                if (snapshot.childrenCount < 1){
                    val tambahData = databaseRef.child("Daftar Nama")
                        .child(nama)
                        .setValue(data)
                    tambahData.addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            toastData("$nama telah ditambahkan dalam database")
                        } else {
                            toastData("$nama gagal di tambahkan")
                        }
                    }
                } else {
                    toastData("Data tersebut sudah ada di database")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                toastData("Terjadi error saat menambah data")
            }

        }

        databaseRef.child("Daftar Nama")
            .child(nama).addListenerForSingleValueEvent(dataListener)

    }

    private fun toastData(pesan: String){
        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
    }
}