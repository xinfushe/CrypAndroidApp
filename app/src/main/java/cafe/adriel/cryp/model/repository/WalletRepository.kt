package cafe.adriel.cryp.model.repository

import cafe.adriel.cryp.Analytics
import cafe.adriel.cryp.Const
import cafe.adriel.cryp.model.entity.Wallet
import cafe.adriel.cryp.model.entity.response.BalanceResponse
import cafe.adriel.cryp.model.repository.factory.ServiceFactory
import io.paperdb.Paper
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

object WalletRepository {
    private val walletDb by lazy {
        Paper.book(Const.DB_WALLETS)
    }
    private val walletService by lazy {
        ServiceFactory.newInstance<WalletService>(Const.WALLET_API_BASE_URL)
    }

    fun getAll() = walletDb.allKeys.map { walletDb.read<Wallet>(it) }

    fun getById(id: String) = walletDb.read<Wallet>(id)

    fun addOrUpdate(wallet: Wallet) {
        if(!contains(wallet)){
            Analytics.logAddWallet(wallet.crypto)
        }
        walletDb.write(wallet.id, wallet)
    }

    fun remove(wallet: Wallet) = walletDb.delete(wallet.id).let { !walletDb.contains(wallet.id) }

    fun contains(wallet: Wallet) = walletDb.contains(wallet.id)

    fun count() = walletDb.allKeys.size

//    fun updateBalances() =
//            Observable.fromArray(getAll())
//                .map { it }
//                .flatMapIterable { it }
//                .flatMap { WalletRepository.updateBalance(it) }
//                .toList()

//    fun updateBalance(wallet: Wallet) =
//            if(wallet.crypto.autoRefresh) {
//                // 1 min interval before update balance
//                // to avoid API rate limit and socket timeout
//                val canUpdate = wallet.updatedAt == null || wallet.updatedAt?.before(Dates.now() - 1.minute) == true
//                if(canUpdate){
//                    walletService.getBalance(wallet.crypto.name.toLowerCase(), wallet.publicKey)
//                        .map {
//                            wallet.apply {
//                                balance = it.balance
//                                updatedAt = Dates.now()
//                                addOrUpdate(this)
//                            }
//                        }
//                        .onErrorResumeNext(Observable.fromCallable { wallet })
//                        .subscribeOn(Schedulers.io())
//                } else {
//                    Observable.fromCallable { wallet }
//                }
//            } else {
//                Observable.fromCallable { wallet }
//            }

    // https://github.com/adrielcafe/Scryp
    interface WalletService {
        @GET("balance/{crypto}/{address}")
        fun getBalance(
                @Path("crypto") crypto: String,
                @Path("address") publicKey: String):
                Observable<BalanceResponse>
    }

}