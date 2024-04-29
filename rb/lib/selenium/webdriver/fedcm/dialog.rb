module FedCM
  module Dialog
    DIALOG_TYPE_ACCOUNT_LIST = "AccountChooser".freeze
    DIALOG_TYPE_AUTO_REAUTH = "AutoReauthn".freeze

    # Closes the dialog as if the user had clicked X.
    def cancel
      raise NotImplementedError, "#{self.class} has not implemented method '#{__method__}'"
    end

    # Selects an account as if the user had clicked on it.
    #
    # @param [Integer] index The index of the account to select from the list returned by get_accounts.
    def select_account(index)
      raise NotImplementedError, "#{self.class} has not implemented method '#{__method__}'"
    end

    # Returns the type of the open dialog.
    #
    # One of DIALOG_TYPE_ACCOUNT_LIST and DIALOG_TYPE_AUTO_REAUTH.
    def type
      raise NotImplementedError, "#{self.class} has not implemented method '#{__method__}'"
    end

    # Returns the title of the dialog.
    def title
      raise NotImplementedError, "#{self.class} has not implemented method '#{__method__}'"
    end

    # Returns the subtitle of the dialog or nil if none.
    def subtitle
      raise NotImplementedError, "#{self.class} has not implemented method '#{__method__}'"
    end

    # Returns the accounts shown in the account chooser.
    #
    # If this is an auto reauth dialog, returns the single account that is being signed in.
    def accounts
      raise NotImplementedError, "#{self.class} has not implemented method '#{__method__}'"
    end
  end
end
