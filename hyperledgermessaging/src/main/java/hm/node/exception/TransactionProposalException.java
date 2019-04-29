package hm.node.exception;

public class TransactionProposalException extends Exception {

   private static final long serialVersionUID = 3648946471509307833L;

   public TransactionProposalException() {
      super();
   }

   public TransactionProposalException(final String message) {
      super(message);
   }

   //TODO can remove this as duplicate of ProposalException
}
