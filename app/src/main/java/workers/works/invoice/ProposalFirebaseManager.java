package workers.works.invoice;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Firebase Manager for Invoice Proposals
 * Handles all database operations for quotations/proposals
 */
public class ProposalFirebaseManager {

    private static final String TAG = "ProposalFirebaseManager";
    private static final String COLLECTION_PROPOSALS = "invoiceProposals";

    private FirebaseFirestore db;

    public ProposalFirebaseManager() {
        db = FirebaseFirestore.getInstance();
    }
    public void submitProposal(InvoiceProposalModel proposal, OnProposalSubmitListener listener) {

        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            if (listener != null) {
                listener.onFailure("User not logged in");
            }
            return;
        }

        DocumentReference docRef = db.collection("WorkerInput").document();
        proposal.setProposalId(docRef.getId());
        proposal.setUserId(userId);   // ✅ SAVE USER ID TO FIREBASE

        docRef.set(proposal)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Proposal submitted successfully: " + proposal.getProposalId());
                    if (listener != null) {
                        listener.onSuccess(proposal.getProposalId());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error submitting proposal", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public void getPendingProposalsForClient(String clientId, OnProposalsLoadListener listener) {
        db.collection(COLLECTION_PROPOSALS)
                .whereEqualTo("userId", clientId)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<InvoiceProposalModel> proposals = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        InvoiceProposalModel proposal = document.toObject(InvoiceProposalModel.class);
                        proposals.add(proposal);
                    }
                    if (listener != null) {
                        listener.onSuccess(proposals);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading client proposals", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Get all proposals sent by a worker
     */
    public void getProposalsByWorker(String workerId, OnProposalsLoadListener listener) {
        db.collection(COLLECTION_PROPOSALS)
                .whereEqualTo("workerId", workerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<InvoiceProposalModel> proposals = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        InvoiceProposalModel proposal = document.toObject(InvoiceProposalModel.class);
                        proposals.add(proposal);
                    }
                    if (listener != null) {
                        listener.onSuccess(proposals);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading worker proposals", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Get accepted proposals for worker (these become Active jobs)
     */
    public void getAcceptedProposalsForWorker(String workerId, OnProposalsLoadListener listener) {
        db.collection(COLLECTION_PROPOSALS)
                .whereEqualTo("workerId", workerId)
                .whereEqualTo("status", "accepted")
                .orderBy("respondedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<InvoiceProposalModel> proposals = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        InvoiceProposalModel proposal = document.toObject(InvoiceProposalModel.class);
                        proposals.add(proposal);
                    }
                    if (listener != null) {
                        listener.onSuccess(proposals);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading accepted proposals", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Client accepts a proposal
     */
    public void acceptProposal(String proposalId, String responseMessage, OnStatusUpdateListener listener) {
        db.collection(COLLECTION_PROPOSALS).document(proposalId)
                .update(
                        "status", "accepted",
                        "respondedAt", Timestamp.now(),
                        "responseMessage", responseMessage
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Proposal accepted: " + proposalId);
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error accepting proposal", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Client declines a proposal
     */
    public void declineProposal(String proposalId, String responseMessage, OnStatusUpdateListener listener) {
        db.collection(COLLECTION_PROPOSALS).document(proposalId)
                .update(
                        "status", "declined",
                        "respondedAt", Timestamp.now(),
                        "responseMessage", responseMessage
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Proposal declined: " + proposalId);
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error declining proposal", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Get a single proposal by ID
     */
    public void getProposalById(String proposalId, OnProposalLoadListener listener) {
        db.collection(COLLECTION_PROPOSALS).document(proposalId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        InvoiceProposalModel proposal = documentSnapshot.toObject(InvoiceProposalModel.class);
                        if (listener != null) {
                            listener.onSuccess(proposal);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure("Proposal not found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading proposal", e);
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Listen to real-time updates for worker's proposals
     */
    public void listenToWorkerProposals(String workerId, OnProposalsLoadListener listener) {
        db.collection(COLLECTION_PROPOSALS)
                .whereEqualTo("workerId", workerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        if (listener != null) {
                            listener.onFailure(error.getMessage());
                        }
                        return;
                    }

                    List<InvoiceProposalModel> proposals = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            InvoiceProposalModel proposal = document.toObject(InvoiceProposalModel.class);
                            proposals.add(proposal);
                        }
                    }
                    if (listener != null) {
                        listener.onSuccess(proposals);
                    }
                });
    }

    // Callback Interfaces
    public interface OnProposalSubmitListener {
        void onSuccess(String proposalId);
        void onFailure(String error);
    }

    public interface OnProposalsLoadListener {
        void onSuccess(List<InvoiceProposalModel> proposals);
        void onFailure(String error);
    }

    public interface OnProposalLoadListener {
        void onSuccess(InvoiceProposalModel proposal);
        void onFailure(String error);
    }

    public interface OnStatusUpdateListener {
        void onSuccess();
        void onFailure(String error);
    }
}
