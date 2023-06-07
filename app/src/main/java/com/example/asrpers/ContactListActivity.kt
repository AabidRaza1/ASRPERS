package com.example.asrpers
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide

class ContactListActivity : AppCompatActivity() {

    private lateinit var listViewContacts: ListView
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        listViewContacts = findViewById(R.id.listViewContacts)
        contactAdapter = ContactAdapter(this)
        listViewContacts.adapter = contactAdapter


        listViewContacts.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val contact = contactAdapter.getItem(position) as Contact

                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(contact.name)
                dialog.setMessage("Choose an action")

                // Add "Delete" option
                dialog.setPositiveButton("Delete") { dialogInterface, _ ->
                    deleteContact(contact)
                }

                // Add "Call" option
                dialog.setNegativeButton("Call") { dialogInterface, _ ->
                    val contact = contactAdapter.getItem(position) as Contact

                    val intent = Intent(this, CallActivity::class.java)
                    intent.putExtra("name", contact.name)
                    intent.putExtra("image", contact.image)
                    intent.putExtra("phone", contact.phone)
                    // Add any other contact details to the intent
                    startActivity(intent)

                }

                dialog.show()
            }

        database = FirebaseDatabase.getInstance().getReference("contacts")
            .child(FirebaseAuth.getInstance().currentUser?.uid.orEmpty())

        loadContactsFromDatabase()

        val fabAddContact: View = findViewById(R.id.fabAddContact)
        fabAddContact.setOnClickListener {
            // TODO: Implement logic to add a new contact
            Toast.makeText(this, "CLICK", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, AddContactActivity::class.java)
            startActivity(intent)

        }
    }

    private fun loadContactsFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val contacts: MutableList<Contact> = mutableListOf()
                for (snapshot in dataSnapshot.children) {
                    val contact = snapshot.getValue(Contact::class.java)
                    if (contact != null) {
                        contacts.add(contact)
                    }
                }
                contactAdapter.updateContacts(contacts)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Toast.makeText(this@ContactListActivity, "DATABASE ERROR", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun refreshContactList() {
        loadContactsFromDatabase()
    }

    private fun deleteContact(contact: Contact) {
        database.child(contact.id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Contact deleted successfully
                Toast.makeText(this@ContactListActivity, "Contact deleted successfully", Toast.LENGTH_SHORT).show()
                refreshContactList()
            } else {
                // Failed to delete contact
                Toast.makeText(this@ContactListActivity, "Failed to delete contact", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

data class Contact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val image: String = ""
)


class ContactAdapter(private val context: Context) : BaseAdapter() {

    private val contactList: MutableList<Contact> = mutableListOf()

    override fun getCount(): Int {
        return contactList.size
    }

    override fun getItem(position: Int): Any {
        return contactList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_contact, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val contact = contactList[position]

        holder.nameTextView.text = contact.name
        holder.phoneTextView.text = contact.phone

        Glide.with(context)
            .load(contact.image)
            .placeholder(R.mipmap.ic_add) // Add a placeholder image resource
            .into(holder.imageView)

        return view
    }


    fun updateContacts(contacts: List<Contact>) {
        contactList.clear()
        contactList.addAll(contacts)
        notifyDataSetChanged()
    }

    private class ViewHolder(view: View) {
        val nameTextView: TextView = view.findViewById(R.id.textViewName)
        val phoneTextView: TextView = view.findViewById(R.id.textViewPhone)
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }
}