package com.afollestad.impression;

import android.app.Activity;
import android.app.Application;

import com.afollestad.impression.accounts.base.Account;
import com.afollestad.impression.utils.PrefUtils;

import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.functions.Func1;

public class App extends Application {

    private Account mCurrentAccount;

    public static Single<Account> getCurrentAccount(Activity context) {
        return ((App) context.getApplication()).getCurrentAccount();
    }

    private Single<Account> getCurrentAccount() {
        final int activeId = PrefUtils.getActiveAccountId(App.this);
        return Observable.create(new Observable.OnSubscribe<Account[]>() {
            @Override
            public void call(Subscriber<? super Account[]> subscriber) {
                if (mCurrentAccount != null && mCurrentAccount.id() == activeId) {
                    subscriber.onNext(new Account[]{mCurrentAccount});
                }
                subscriber.onCompleted();
            }
        }).switchIfEmpty(Account.getAll(this).toObservable())
                .map(new Func1<Account[], Account>() {
                    @Override
                    public Account call(Account[] accounts) {
                        for (Account a : accounts) {
                            if (a.id() == activeId) {
                                mCurrentAccount = a;
                                break;
                            }
                        }
                        return mCurrentAccount;
                    }
                }).toSingle();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //LeakCanary.install(this);
    }
}